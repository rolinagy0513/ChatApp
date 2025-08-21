package org.example.chatapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chatapp.DTO.*;
import org.example.chatapp.ENUM.FriendLiveStatus;
import org.example.chatapp.ENUM.FriendRequestStatus;
import org.example.chatapp.ENUM.ResponseStatus;
import org.example.chatapp.exception.*;
import org.example.chatapp.model.FriendRequest;
import org.example.chatapp.model.FriendShip;
import org.example.chatapp.repository.FriendRequestRepository;
import org.example.chatapp.repository.FriendShipRepository;
import org.example.chatapp.security.user.User;
import org.example.chatapp.security.user.UserRepository;
import org.example.chatapp.security.user.UserService;
import org.example.chatapp.service.IFriendsService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for managing friendships and friend requests.
 * <p>
 * Provides functionality for:
 * <ul>
 *     <li>Sending and responding to friend requests</li>
 *     <li>Creating and removing friendships</li>
 *     <li>Retrieving friendship and friend request information</li>
 *     <li>Validating user and friendship state</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService implements IFriendsService {

    private final FriendShipRepository repository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CacheService cacheService;
    private final PresenceService presenceService;

    /**
     * Sends a friend request from the authenticated user to the specified recipient.
     * Validates users and prevents duplicate or invalid requests.
     * Notifies the recipient via WebSocket.
     *
     * @param recipientId the ID of the recipient
     * @param principal   the authenticated user's principal
     * @throws InvalidFriendRequestException if the request is invalid
     */
    @Transactional
    public void sendFriendRequest(Long recipientId, Principal principal) {

        User sender = userService.getUserFromPrincipal(principal);
        Long senderId = sender.getId();

        if (senderId.equals(recipientId)) {
            throw new InvalidFriendRequestException("Cannot send friend request to yourself");
        }

        User recipient = validateUser(recipientId);

        if (areFriends(sender.getId(),recipient.getId())){
            throw new InvalidFriendRequestException("Users are already friends");
        }

        if (friendRequestRepository.existsBySenderAndRecipientAndStatus(sender,recipient,FriendRequestStatus.PENDING)){
            throw new InvalidFriendRequestException("Friend request is already sent");
        }

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(FriendRequestStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        FriendRequest savedRequest = friendRequestRepository.save(request);

        String recipientUserId = savedRequest.getRecipient().getId().toString();

        FriendRequestNotification notification = FriendRequestNotification.builder()
                .id(request.getId())
                .senderId(savedRequest.getSender().getId())
                .senderName(savedRequest.getSender().getName())
                .recipientId(savedRequest.getRecipient().getId())
                .content("New Friend request came!")
                .timeStamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSendToUser(
                recipientUserId, "/queue/requests", notification
        );
    }

    /**
     * Responds to a friend request as accepted or rejected.
     * Updates request status, creates a friendship if accepted,
     * and notifies involved users via WebSocket.
     *
     * @param requestId  the friend request ID
     * @param isAccepted the response status
     * @param principal  the authenticated user's principal
     */
    @Transactional
    public void sendRequestResponse(Long requestId, ResponseStatus isAccepted, Principal principal){

        User currentUser = userService.getUserFromPrincipal(principal);
        Long currentUserId = currentUser.getId();

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(()-> new RequestNotFoundException(requestId));

        if (!request.getRecipient().getId().equals(currentUserId)){
            throw new InvalidRecipientIdInRequestException("The request was not meant to you!");
        }

       User originalSender = validateUser(request.getSender().getId());

        if (isAccepted == ResponseStatus.ACCEPTED){
            request.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequestRepository.save(request);
            String originalSenderString = request.getSender().getId().toString();
            String originalRecipientString = request.getRecipient().getId().toString();

            RequestResponseNotification notificationForOriginalSender = RequestResponseNotification.builder()
                    .senderId(request.getSender().getId())
                    .senderName(request.getRecipient().getName())
                    .recipientId(request.getRecipient().getId())
                    .content("Accepted your friend request")
                    .status(ResponseStatus.ACCEPTED)
                    .friendsSince(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    originalSenderString, "/queue/request-responses", notificationForOriginalSender
            );

            RequestResponseNotification notificationForOriginalRecipient = RequestResponseNotification.builder()
                    .senderId(request.getSender().getId())
                    .senderName(request.getSender().getName())
                    .recipientId(request.getRecipient().getId())
                    .content("You have accepted a friend request")
                    .status(ResponseStatus.ACCEPTED)
                    .friendsSince(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    originalRecipientString,"/queue/request-responses", notificationForOriginalRecipient
            );

            FriendShipResponseDTO friendship = makeFriendship(currentUser,originalSender);

        }else{
            request.setStatus(FriendRequestStatus.REJECTED);
            friendRequestRepository.save(request);
            String originalSenderString = request.getSender().getId().toString();

            RequestResponseNotification notification = RequestResponseNotification.builder()
                    .senderId(request.getSender().getId())
                    .senderName(request.getSender().getName())
                    .recipientId(request.getRecipient().getId())
                    .content("Rejected your friend request")
                    .status(ResponseStatus.REJECTED)
                    .build();

            messagingTemplate.convertAndSendToUser(
                    originalSenderString, "/queue/request-responses", notification
            );
        }
    }

    /**
     * Creates a friendship between two users and clears related cache entries.
     *
     * @param sender    the initiating user
     * @param recipient the receiving user
     * @return the created friendship as a DTO
     */
    @Transactional
    public FriendShipResponseDTO makeFriendship(User sender, User recipient){


        if (sender.getId().equals(recipient.getId())) {
            throw new InvalidFriendRequestException("Cannot send friend request to yourself");
        }

        String senderEmail = sender.getEmail();
        String recipientEmail = recipient.getEmail();

        FriendShip friendShip = FriendShip.builder()
                .user1(sender)
                .user2(recipient)
                .createdAt(LocalDateTime.now())
                .build();

        FriendShip savedFriendShip = repository.save(friendShip);

        cacheService.evictFriendsCache(senderEmail);
        cacheService.evictFriendsCache(recipientEmail);

        return FriendShipResponseDTO.builder()
                .id(friendShip.getId())
                .initiatorName(friendShip.getUser1().getName())
                .recipientName(friendShip.getUser2().getName())
                .friendsSince(friendShip.getCreatedAt())
                .build();
    }

    /**
     * Removes the friendship between the authenticated user and another user.
     * Clears cache entries and notifies both users via WebSocket.
     *
     * @param otherUserId the ID of the other user
     */
    @Transactional
    public void removeFriendship(Long otherUserId){

        Long authUserId = userService.getCurrentUser().getId();

        User authUser = validateUser(authUserId);
        User otherUser = validateUser(otherUserId);

        if (!areFriends(authUser.getId(),otherUser.getId())){
            throw new UsersAreNotFriendsException("Users are not friends");
        }

        String authUserEmail = authUser.getEmail();
        String otherUserEmail = otherUser.getEmail();

        List<FriendShip> friendShips = repository.findFriendshipBetweenUsers(authUserId,otherUserId);
        repository.deleteAll(friendShips);

        cacheService.evictFriendsCache(authUserEmail);
        cacheService.evictFriendsCache(otherUserEmail);

        String authUserIdString = authUserId.toString();
        String otherUserIdString = otherUserId.toString();

        RemovalDTO removalDTO = RemovalDTO.builder()
                .message("Friendship removed")
                .build();

        messagingTemplate.convertAndSendToUser(
                authUserIdString,"/queue/friendRemoval", removalDTO
        );


        messagingTemplate.convertAndSendToUser(
                otherUserIdString,"/queue/friendRemoval",removalDTO
        );

    }


    /**
     * Checks if two users are friends.
     *
     * @param user1Id the first user's ID
     * @param user2Id the second user's ID
     * @return {@code true} if they are friends, otherwise {@code false}
     */
    boolean areFriends(Long user1Id, Long user2Id) {
        return repository.existsFriendshipBetweenUsers(user1Id, user2Id);
    }

    /**
     * Retrieves all friendships for the authenticated user, including live status.
     * Results are cached for faster access.
     *
     * @param principal the authenticated user's principal
     * @return a list of friendships as DTOs
     */
    @Async("asyncExecutor")
    @Cacheable(value = "friends", key = "#principal.name")
    public CompletableFuture<List<FriendShipDTO>> getAllFriendships(Principal principal) {
        User currentUser = userService.getUserFromPrincipal(principal);
        Long userId = currentUser.getId();

        List<FriendShip> friendShips = repository.findByUserIdWithUsers(userId);

        Set<String> friendEmails = friendShips.stream()
                .map(friendShip ->
                        friendShip.getUser1().getId().equals(userId)
                                ? friendShip.getUser2().getEmail()
                                : friendShip.getUser1().getEmail()
                )
                .collect(Collectors.toSet());

        Map<String, Boolean> onlineStatusMap = presenceService.getBulkStatus(friendEmails);

        List<FriendShipDTO> result = friendShips.stream()
                .map(friendShip -> {
                    User otherUser = friendShip.getUser1().getId().equals(userId)
                            ? friendShip.getUser2()
                            : friendShip.getUser1();

                    return FriendShipDTO.builder()
                            .id(otherUser.getId())
                            .userName(otherUser.getName())
                            .friendsSince(friendShip.getCreatedAt())
                            .liveStatus(onlineStatusMap.getOrDefault(otherUser.getEmail(), false)
                                    ? FriendLiveStatus.ONLINE
                                    : FriendLiveStatus.OFFLINE)
                            .build();
                })
                .toList();

        return CompletableFuture.completedFuture(result);
    }

    /**
     * Retrieves detailed information about a specific friend.
     *
     * @param friendId the friend's ID
     * @return detailed friend information as a DTO
     * @throws NotFriendsException if the users are not friends
     */
    public FriendInformationDTO getOneFriend(Long friendId){

        UserDTO currentUser = userService.getCurrentUser();
        User friend = validateUser(friendId);

        LocalDateTime friendsSince = repository.findFriendshipCreatedAtBetweenUsers(
                        currentUser.getId(),
                        friendId
                )
                .orElseThrow(() -> new NotFriendsException(friendId));

       return FriendInformationDTO.builder()
               .userName(friend.getName())
               .liveStatus(presenceService.isOnline(
                       friend.getEmail())
                       ? FriendLiveStatus.ONLINE
                       : FriendLiveStatus.OFFLINE
               )
               .firstName(friend.getFirstname())
               .lastName(friend.getLastname())
               .email(friend.getEmail())
               .friendsSince(friendsSince)
               .build();

    }

    /**
     * Retrieves all pending friend requests for the authenticated user.
     *
     * @return a list of pending friend requests as DTOs
     */

    public List<FriendRequestDTO> getAllRequestForUser() {

        Long userId = userService.getCurrentUser().getId();

        List<FriendRequest> requests = friendRequestRepository.findPendingRequestsByRecipientId(userId);

        return requests.stream()
                .map(request -> FriendRequestDTO.builder()
                        .id(request.getId())
                        .senderId(request.getSender().getId())
                        .senderName(request.getSender().getName())
                        .sentAt(request.getSentAt())
                        .build())
                .toList();
    }

    /**
     * Searches for users by username, excluding the authenticated user.
     *
     * @param userName the username to search
     * @return an optional list of matching users as DTOs
     */

    public Optional<List<UserDTO>> findUsersByUserName(String userName) {
        UserDTO currentUser = userService.getCurrentUser();
        Long currentUserId = currentUser.getId();

        List<User> users = userRepository.flexibleSearch(userName,currentUserId);

        if (users.isEmpty()) {
            return Optional.empty();
        }

        List<UserDTO> dtoList = users.stream()
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .userName(user.getName())
                        .build())
                .toList();

        return Optional.of(dtoList);
    }

    /**
     * Validates that a user with the given ID exists.
     *
     * @param userId the user ID
     * @return the user entity
     * @throws UserIdNotFoundException if no user is found
     */

    public User validateUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()-> new UserIdNotFoundException(userId));
    }
}