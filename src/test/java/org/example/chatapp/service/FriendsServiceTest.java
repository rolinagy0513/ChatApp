package org.example.chatapp.service;

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
import org.example.chatapp.service.impl.CacheService;
import org.example.chatapp.service.impl.FriendService;
import org.example.chatapp.service.impl.PresenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FriendService}.
 * <p>
 * This class is responsible for sending request and responses to requests via websocket
 * Making users friends. fetching the users friendsList and requests
 * <ul>
 *   <li>The request has valid data and properly sent out</li>
 *   <li>The response has valid data and properly sent out</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class FriendsServiceTest {

    @Mock
    private FriendShipRepository friendShipRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private CacheService cacheService;

    @Mock
    private PresenceService presenceService;

    @InjectMocks
    FriendService friendService;

    private User sender;
    private User recipient;
    private Principal mockPrincipal;

    @BeforeEach
    void setup() {
        mockPrincipal = mock(Principal.class);

        sender = User.builder()
                .id(1L)
                .userName("Sender user")
                .email("sender@user.com")
                .firstname("Sender")
                .lastname("user")
                .build();

        recipient = User.builder()
                .id(2L)
                .userName("Recipient user")
                .email("recipient@user.com")
                .firstname("Recipient")
                .lastname("user")
                .build();
    }

    /**
     * This test verifies that when a request with the correct valid data is sent out the
     * user is receiving the notification with websocket
     */
    @Test
    void sendFriendRequest_WithValidData_ShouldSendRequest() {

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(sender);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(friendShipRepository.existsFriendshipBetweenUsers(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.existsBySenderAndRecipientAndStatus(sender, recipient, FriendRequestStatus.PENDING))
                .thenReturn(false);

        FriendRequest savedRequest = FriendRequest.builder()
                .id(1L)
                .sender(sender)
                .recipient(recipient)
                .status(FriendRequestStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(savedRequest);

        friendService.sendFriendRequest(recipient.getId(), mockPrincipal);

        verify(friendRequestRepository).save(any(FriendRequest.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq(recipient.getId().toString()),
                eq("/queue/requests"),
                any(FriendRequestNotification.class)
        );
    }

    /**
     * This test is for the scenario where the current users id is the same as the recipient id
     * where the request is heading.
     */
    @Test
    void sendFriendRequest_WhenSenderIdEqualsRecipient_ShouldThrowInvalidFriendRequestException() {

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(sender);
        Long senderIdAsRecipient = sender.getId();

        InvalidFriendRequestException exception = assertThrows(
                InvalidFriendRequestException.class,
                () -> friendService.sendFriendRequest(senderIdAsRecipient, mockPrincipal)
        );

        assertEquals("Cannot send friend request to yourself", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(friendRequestRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    /**
     * This is a test for a scenario where the provided id is not a real userId
     */
    @Test
    void sendFriendRequest_WhenUserValidationFails_ShouldThrowUserIdNotFoundException() {

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(sender);
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        UserIdNotFoundException exception = assertThrows(
                UserIdNotFoundException.class,
                () -> friendService.sendFriendRequest(nonExistentUserId, mockPrincipal)
        );

        verify(friendRequestRepository, never()).existsBySenderAndRecipientAndStatus(any(), any(), any());
        verify(friendRequestRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    /**
     * This is a test for the scenario where the users are already friends
     */
    @Test
    void sendFriendRequest_WhenUsersAreAlreadyFriends_ShouldThrowInvalidFriendRequestException() {

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(sender);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(friendShipRepository.existsFriendshipBetweenUsers(1L, 2L)).thenReturn(true);

        InvalidFriendRequestException exception = assertThrows(
                InvalidFriendRequestException.class,
                () -> friendService.sendFriendRequest(recipient.getId(), mockPrincipal)
        );

        assertEquals("Users are already friends", exception.getMessage());

        verify(friendRequestRepository, never()).existsBySenderAndRecipientAndStatus(any(), any(), any());
        verify(friendRequestRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    /**
     * A test for the scenario where the request was already sent out
     */
    @Test
    void sendFriendRequest_WhenFriendRequestAlreadySent_ShouldThrowInvalidFriendRequestException() {

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(sender);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(friendShipRepository.existsFriendshipBetweenUsers(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.existsBySenderAndRecipientAndStatus(sender, recipient, FriendRequestStatus.PENDING))
                .thenReturn(true);

        InvalidFriendRequestException exception = assertThrows(
                InvalidFriendRequestException.class,
                () -> friendService.sendFriendRequest(recipient.getId(), mockPrincipal)
        );

        assertEquals("Friend request is already sent", exception.getMessage());

        verify(friendRequestRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    /**
     * A test for the happy path of the response to a certain request
     * In this case the request is accepted
     */
    @Test
    void sendRequestResponse_WithValidData_AcceptedRequest_ShouldSendResponse(){

        User currentUser = User.builder()
                .id(3L)
                .build();

        FriendRequest existingRequest = FriendRequest.builder()
                .id(1L)
                .sender(sender)
                .recipient(currentUser)
                .status(FriendRequestStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        FriendShip friendShip = FriendShip.builder()
                .user1(sender)
                .user2(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(currentUser);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(userRepository.findById(existingRequest.getSender().getId())).thenReturn(Optional.of(sender));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(existingRequest);
        when(friendShipRepository.save(any(FriendShip.class))).thenReturn(friendShip);

        friendService.sendRequestResponse(existingRequest.getId(), ResponseStatus.ACCEPTED,mockPrincipal);

        assertEquals(FriendRequestStatus.ACCEPTED,existingRequest.getStatus());
        assertEquals(sender.getId(),friendShip.getUser1().getId());
        assertEquals(currentUser.getId(),friendShip.getUser2().getId());

        verify(userService).getUserFromPrincipal(mockPrincipal);
        verify(friendRequestRepository).findById(1L);
        verify(userRepository).findById(existingRequest.getSender().getId());
        verify(friendRequestRepository).save(any(FriendRequest.class));

        verify(messagingTemplate).convertAndSendToUser(
                eq(existingRequest.getSender().getId().toString()),
                eq("/queue/request-responses"),
                any(RequestResponseNotification.class)
        );

        verify(friendShipRepository).save(any(FriendShip.class));

    }

    /**
     * A test for the happy path of the response to a certain request
     * In this case the request is rejected
     */
    @Test
    void sendRequestResponse_WitValidData_RejectedRequest_ShouldSendResponse(){

        User currentUser = User.builder()
                .id(3L)
                .build();

        FriendRequest existingRequest = FriendRequest.builder()
                .id(1L)
                .sender(sender)
                .recipient(currentUser)
                .status(FriendRequestStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(currentUser);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(userRepository.findById(existingRequest.getSender().getId())).thenReturn(Optional.of(sender));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(existingRequest);

        friendService.sendRequestResponse(existingRequest.getId(),ResponseStatus.REJECTED,mockPrincipal);

        assertEquals(FriendRequestStatus.REJECTED,existingRequest.getStatus());

        verify(userService).getUserFromPrincipal(mockPrincipal);
        verify(friendRequestRepository).findById(1L);
        verify(userRepository).findById(existingRequest.getSender().getId());
        verify(friendRequestRepository).save(any(FriendRequest.class));

        verify(messagingTemplate).convertAndSendToUser(
                eq(existingRequest.getSender().getId().toString()),
                eq("/queue/request-responses"),
                any(RequestResponseNotification.class)
        );

        verify(friendShipRepository,never()).save(any(FriendShip.class));
    }

    /**
     * This test simulates the scenario where the requestId is invalid
     */
    @Test
    void sendRequestResponse_WithInvalidRequestId_ShouldThrowRequestNotFoundException(){

        User currentUser = User.builder()
                .id(3L)
                .build();

        Long invalidRequestId = 99L;
        Long fakeUserId = 99L;

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(currentUser);
        when(friendRequestRepository.findById(99L)).thenThrow(RequestNotFoundException.class);

        assertThrows(RequestNotFoundException.class,()->{
            friendService.sendRequestResponse(invalidRequestId,ResponseStatus.ACCEPTED,mockPrincipal);
        });

        verify(userService).getUserFromPrincipal(mockPrincipal);
        verify(friendRequestRepository).findById(invalidRequestId);

        verify(messagingTemplate,never()).convertAndSendToUser(
                eq(fakeUserId.toString()),
                eq("/queue/request-responses"),
                any(RequestResponseNotification.class)
        );

        verify(userRepository,never()).findById(fakeUserId);
        verify(friendRequestRepository,never()).save(any(FriendRequest.class));

    }

    /**
     * This is a scenario where the recipientId is invalid
     */
    @Test
    void sendRequestResponse_WithInvalidRecipientId_ShouldThrowInvalidRecipientIdInRequestException(){

        User currentUser = User.builder()
                .id(3L)
                .build();

        User otherUser = User.builder()
                .id(5L)
                .build();

        FriendRequest existingRequest = FriendRequest.builder()
                .id(1L)
                .sender(sender)
                .recipient(otherUser)
                .status(FriendRequestStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        String expectedErrorText = "The request was not meant to you!";

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(currentUser);
        when(friendRequestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));

        InvalidRecipientIdInRequestException exception = assertThrows(
                InvalidRecipientIdInRequestException.class,
                () -> friendService.sendRequestResponse(existingRequest.getId(), ResponseStatus.ACCEPTED, mockPrincipal)
        );

        assertEquals(FriendRequestStatus.PENDING,existingRequest.getStatus());
        assertEquals(expectedErrorText,exception.getMessage());

        verify(userService).getUserFromPrincipal(mockPrincipal);
        verify(friendRequestRepository).findById(existingRequest.getId());

        verify(messagingTemplate,never()).convertAndSendToUser(
                eq(sender.getId().toString()),
                eq("/queue/request-responses"),
                any(RequestResponseNotification.class)
        );

        verify(userRepository,never()).findById(sender.getId());
        verify(friendRequestRepository,never()).save(any(FriendRequest.class));

    }

    /**
     * This scenario simulates the case where a user tries to send a request to themselves
     */
    @Test
    void sendRequestResponse_WithSameUsersId_ShouldThrowInvalidFriendRequestException(){


        User currentUser = User.builder()
                .id(3L)
                .build();

        User otherUser = User.builder()
                .id(3L)
                .build();

        FriendRequest existingRequest = FriendRequest.builder()
                .id(1L)
                .sender(otherUser)
                .recipient(currentUser)
                .status(FriendRequestStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        String expectedErrorText = "Cannot send friend request to yourself";

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(currentUser);
        when(friendRequestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        InvalidFriendRequestException exception = assertThrows(
                InvalidFriendRequestException.class,
                () -> friendService.sendRequestResponse(existingRequest.getId(), ResponseStatus.ACCEPTED, mockPrincipal)
        );

        assertEquals(expectedErrorText,exception.getMessage());

        verify(userService).getUserFromPrincipal(mockPrincipal);
        verify(friendRequestRepository).findById(existingRequest.getId());
        verify(userRepository).findById(otherUser.getId());

        verify(messagingTemplate,times(2)).convertAndSendToUser(
                eq(otherUser.getId().toString()),
                eq("/queue/request-responses"),
                any(RequestResponseNotification.class)
        );

        verify(friendShipRepository,never()).save(any(FriendShip.class));
    }

    /**
     * This is a happy path of the removal method which removes a certain user
     * from the friends list
     */
    @Test
    void removeFriendShip_WithValidData_ShouldRemoveTheFriendship() {

        User currentUser = User.builder()
                .id(1L)
                .email("authUser@user.com")
                .build();

        User otherUser = User.builder()
                .id(2L)
                .email("otherUser@user.com")
                .build();

        FriendShip friendship = FriendShip.builder()
                .id(1L)
                .user1(currentUser)
                .user2(otherUser)
                .build();

        List<FriendShip> friendShipList = List.of(friendship);

        when(userService.getCurrentUser()).thenReturn(UserDTO.builder().id(1L).build());
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(friendShipRepository.existsFriendshipBetweenUsers(1L, 2L)).thenReturn(true);
        when(friendShipRepository.findFriendshipBetweenUsers(1L, 2L)).thenReturn(friendShipList);

        friendService.removeFriendship(2L);

        verify(userService).getCurrentUser();
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(friendShipRepository).deleteAll(friendShipList);
        verify(cacheService).evictFriendsCache("authUser@user.com");
        verify(cacheService).evictFriendsCache("otherUser@user.com");

        verify(messagingTemplate).convertAndSendToUser(
                eq("1"),
                eq("/queue/friendRemoval"),
                any(RemovalDTO.class)
        );

        verify(messagingTemplate).convertAndSendToUser(
                eq("2"),
                eq("/queue/friendRemoval"),
                any(RemovalDTO.class)
        );
    }

    /**
     * This is a test for the removal of a friend
     * where the provided id is invalid
     */
    @Test
    void removeFriendShip_WithInvalidUserId_ShouldThrowUserIdNotFoundException(){

        User currentUser = User.builder()
                .id(1L)
                .email("authUser@user.com")
                .build();

        Long invalidUserId = 99L;

        when(userService.getCurrentUser()).thenReturn(UserDTO.builder().id(1L).build());
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(invalidUserId)).thenThrow(UserIdNotFoundException.class);

        assertThrows(UserIdNotFoundException.class,()->{
            friendService.removeFriendship(invalidUserId);
        });

        verify(userService).getCurrentUser();
        verify(userRepository).findById(1L);
        verify(userRepository).findById(invalidUserId);

        verify(friendShipRepository,never()).deleteAll();
        verify(cacheService,never()).evictFriendsCache("authUser@user.com");
        verify(cacheService,never()).evictFriendsCache("otherUser@user.com");

        verify(messagingTemplate,never()).convertAndSendToUser(
                eq("1"),
                eq("/queue/friendRemoval"),
                any(RemovalDTO.class)
        );

        verify(messagingTemplate,never()).convertAndSendToUser(
                eq("2"),
                eq("/queue/friendRemoval"),
                any(RemovalDTO.class)
        );
    }

    /**
     * This scenario is when the user tries to remove
     * a non friend user from the friends list
     */
    @Test
    void removeFriendShip_WhenUsersAreNotFriends_ShouldThrowUsersAreNotFriendsException(){

        User currentUser = User.builder()
                .id(1L)
                .email("authUser@user.com")
                .build();

        User otherUser = User.builder()
                .id(2L)
                .email("otherUser@user.com")
                .build();

        String errorMessage = "Users are not friends";

        when(userService.getCurrentUser()).thenReturn(UserDTO.builder().id(1L).build());
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(friendShipRepository.existsFriendshipBetweenUsers(currentUser.getId(), otherUser.getId())).thenReturn(false);

        UsersAreNotFriendsException exception = assertThrows(UsersAreNotFriendsException.class,()->{
            friendService.removeFriendship(otherUser.getId());
        });

        assertEquals(errorMessage,exception.getMessage());

        verify(userService).getCurrentUser();
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(friendShipRepository).existsFriendshipBetweenUsers(currentUser.getId(), otherUser.getId());

        verify(friendShipRepository,never()).findFriendshipBetweenUsers(currentUser.getId(), otherUser.getId());

        verify(friendShipRepository,never()).deleteAll();
        verify(cacheService,never()).evictFriendsCache("authUser@user.com");
        verify(cacheService,never()).evictFriendsCache("otherUser@user.com");

        verify(messagingTemplate,never()).convertAndSendToUser(
                eq("1"),
                eq("/queue/friendRemoval"),
                any(RemovalDTO.class)
        );

        verify(messagingTemplate,never()).convertAndSendToUser(
                eq("2"),
                eq("/queue/friendRemoval"),
                any(RemovalDTO.class)
        );

    }

    /**
     * This method is the happy path of the getAllFriendships
     * it returns the list of friends
     */
    @Test
    void getAllFriendships_WithFriends_ShouldReturnListOfFriends() throws Exception {

        User currentUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        User friend1 = User.builder()
                .id(2L)
                .email("friend1@example.com")
                .userName("Friend One")
                .build();

        User friend2 = User.builder()
                .id(3L)
                .email("friend2@example.com")
                .userName("Friend Two")
                .build();

        FriendShip friendship1 = FriendShip.builder()
                .id(1L)
                .user1(currentUser)
                .user2(friend1)
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();

        FriendShip friendship2 = FriendShip.builder()
                .id(2L)
                .user1(friend2)
                .user2(currentUser)
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        List<FriendShip> friendships = List.of(friendship1, friendship2);

        Set<String> friendEmails = Set.of("friend1@example.com", "friend2@example.com");
        Map<String, Boolean> onlineStatus = Map.of(
                "friend1@example.com", true,
                "friend2@example.com", false
        );

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(currentUser);
        when(friendShipRepository.findByUserIdWithUsers(1L)).thenReturn(friendships);
        when(presenceService.getBulkStatus(friendEmails)).thenReturn(onlineStatus);

        CompletableFuture<List<FriendShipDTO>> resultFuture = friendService.getAllFriendships(mockPrincipal);
        List<FriendShipDTO> result = resultFuture.get();

        assertEquals(2, result.size());

        FriendShipDTO dto1 = result.get(0);
        assertEquals(2L, dto1.getId());
        assertEquals("Friend One", dto1.getUserName());
        assertEquals(FriendLiveStatus.ONLINE, dto1.getLiveStatus());

        FriendShipDTO dto2 = result.get(1);
        assertEquals(3L, dto2.getId());
        assertEquals("Friend Two", dto2.getUserName());
        assertEquals(FriendLiveStatus.OFFLINE, dto2.getLiveStatus());
    }

    /**
     * This is a test where the getAllFriendships is returning an empty list
     */
    @Test
    void getAllFriendships_WithNoFriends_ShouldReturnEmptyList() throws Exception {

        User currentUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        when(userService.getUserFromPrincipal(mockPrincipal)).thenReturn(currentUser);
        when(friendShipRepository.findByUserIdWithUsers(1L)).thenReturn(List.of());

        CompletableFuture<List<FriendShipDTO>> resultFuture = friendService.getAllFriendships(mockPrincipal);
        List<FriendShipDTO> result = resultFuture.get();

        assertEquals(0, result.size());
    }

    /**
     * This test is the happy path of the method which returns information
     * about a friend with the provided id
     */
    @Test
    void getOneFriend_WithValidData_ShouldReturnFriendInformationDTO() {

        User currentUser = User.builder()
                .id(1L)
                .email("currentUser@user.com")
                .firstname("Current")
                .lastname("User")
                .userName("Current User")
                .build();

        User friendUser = User.builder()
                .id(2L)
                .email("friend@user.com")
                .firstname("Friend")
                .lastname("User")
                .userName("Friend User")
                .build();

        LocalDateTime friendsSince = LocalDateTime.now().minusDays(10);

        when(userService.getCurrentUser()).thenReturn(UserDTO.builder()
                .id(1L)
                .userName("Current User")
                .build());

        when(userRepository.findById(2L)).thenReturn(Optional.of(friendUser));

        when(friendShipRepository.findFriendshipCreatedAtBetweenUsers(1L, 2L))
                .thenReturn(Optional.of(friendsSince));

        when(presenceService.isOnline("friend@user.com")).thenReturn(true);

        FriendInformationDTO result = friendService.getOneFriend(2L);

        assertEquals("Friend User", result.getUserName());
        assertEquals(FriendLiveStatus.ONLINE, result.getLiveStatus());
        assertEquals("Friend", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("friend@user.com", result.getEmail());
        assertEquals(friendsSince, result.getFriendsSince());

        verify(userService).getCurrentUser();
        verify(userRepository).findById(2L);
        verify(friendShipRepository).findFriendshipCreatedAtBetweenUsers(1L,2L);
        verify(presenceService).isOnline("friend@user.com");
    }

    /**
     * This method is testing a scenario where the two users are not friends
     * but the user tries to access the data.
     */
    @Test
    void getOneFriend_WhereUsersAreNotFriends_ShouldThrowNotFriendsException(){

        User currentUser = User.builder()
                .id(1L)
                .email("currentUser@user.com")
                .firstname("Current")
                .lastname("User")
                .userName("Current User")
                .build();

        User friendUser = User.builder()
                .id(2L)
                .email("friend@user.com")
                .firstname("Friend")
                .lastname("User")
                .userName("Friend User")
                .build();

        LocalDateTime friendsSince = LocalDateTime.now().minusDays(10);

        when(userService.getCurrentUser()).thenReturn(UserDTO.builder()
                .id(1L)
                .userName("Current User")
                .build());

        when(userRepository.findById(2L)).thenReturn(Optional.of(friendUser));
        when(friendShipRepository.findFriendshipCreatedAtBetweenUsers(currentUser.getId(), friendUser.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFriendsException.class,()->{
            friendService.getOneFriend(friendUser.getId());
        });

        verify(userService).getCurrentUser();
        verify(userRepository).findById(2L);
        verify(friendShipRepository).findFriendshipCreatedAtBetweenUsers(currentUser.getId(), friendUser.getId());

        verify(presenceService,never()).isOnline(friendUser.getEmail());
    }

    /**
     * This test is the happy path of the method
     * which returns all PENDING requests
     */
    @Test
    void  getAllRequestForUser_WithRequests_ShouldReturnRequests(){

        User currentUser = User.builder()
                .id(1L)
                .build();

        User otherUser = User.builder()
                .id(2L)
                .build();

        User thirdUser = User.builder()
                .id(3L)
                .build();

        FriendRequest request1 = FriendRequest.builder()
                .id(1L)
                .status(FriendRequestStatus.PENDING)
                .sender(otherUser)
                .recipient(currentUser)
                .build();

        FriendRequest request2 = FriendRequest.builder()
                .id(2L)
                .status(FriendRequestStatus.PENDING)
                .sender(thirdUser)
                .recipient(currentUser)
                .build();

        List<FriendRequest> requests = List.of(request1,request2);

        when(userService.getCurrentUser()).thenReturn(UserDTO.builder()
                        .id(1L)
                .build());
        when(friendRequestRepository.findPendingRequestsByRecipientId(currentUser.getId())).thenReturn(requests);

        List<FriendRequestDTO> result = friendService.getAllRequestForUser();

        assertEquals(2, result.size());

        FriendRequestDTO dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals(2L, dto1.getSenderId());

        FriendRequestDTO dto2 = result.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals(3L, dto2.getSenderId());

        verify(friendRequestRepository).findPendingRequestsByRecipientId(currentUser.getId());

        verify(userService).getCurrentUser();

    }

    /**
     * This is a test for the scenario where the request list is empty
     */
    @Test
    void getAllRequestForUser_WithNoRequests_ShouldReturnEmptyList() {

        when(userService.getCurrentUser()).thenReturn(UserDTO.builder().id(1L).build());
        when(friendRequestRepository.findPendingRequestsByRecipientId(1L)).thenReturn(List.of());

        List<FriendRequestDTO> result = friendService.getAllRequestForUser();

        assertTrue(result.isEmpty());

        verify(userService).getCurrentUser();
        verify(friendRequestRepository).findPendingRequestsByRecipientId(1L);
    }

    /**
     * This test is the happy path of the method that returns
     * a list of users with the similar name as the provided string
     */
    @Test
    void findUsersByUserName_WithUsers_ShouldReturnUsers(){

        User user1 = User.builder()
                .id(2L)
                .userName("User1")
                .build();

        User user2 = User.builder()
                .id(3L)
                .userName("User2")
                .build();

        List<User> usersList = List.of(user1,user2);

        when(userService.getCurrentUser()).thenReturn(UserDTO.builder().id(1L).build());
        when(userRepository.flexibleSearch("user",1L)).thenReturn(usersList);

        Optional<List<UserDTO>> result = friendService.findUsersByUserName("user");

        assertTrue(result.isPresent());
        assertEquals(2,result.get().size());
        assertEquals(2L, result.get().get(0).getId());
        assertEquals("User1", result.get().get(0).getUserName());
        assertEquals(3L, result.get().get(1).getId());
        assertEquals("User2", result.get().get(1).getUserName());
    }

    /**
     * This is a scenario where the method returns an empty list of users
     */
    @Test
    void findUsersByUserName_WithNoMatchingUsers_ShouldReturnEmptyOptional() {

        when(userService.getCurrentUser()).thenReturn(UserDTO.builder().id(1L).build());
        when(userRepository.flexibleSearch("nonexistent", 1L)).thenReturn(List.of());

        Optional<List<UserDTO>> result = friendService.findUsersByUserName("nonexistent");

        assertFalse(result.isPresent());

    }

}