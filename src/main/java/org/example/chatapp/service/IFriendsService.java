package org.example.chatapp.service;

import org.example.chatapp.DTO.*;
import org.example.chatapp.ENUM.ResponseStatus;
import org.example.chatapp.security.user.User;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for managing friendships between users.
 * <p>
 * Defines operations for sending and responding to friend requests, creating friendships,
 * retrieving friendship data, and validating users.
 * </p>
 * <p>
 *      For more information look at {@link org.example.chatapp.service.impl.FriendService}
 * </p>
 */
public interface IFriendsService {

    void sendFriendRequest(Long recipientId, Principal principal);

    void sendRequestResponse(Long requestId, ResponseStatus isAccepted, Principal principal);

    FriendShipResponseDTO makeFriendship(User sender, User recipient);

    void removeFriendship(Long otherUserId);

    CompletableFuture<List<FriendShipDTO>> getAllFriendships(Principal principal);

    FriendInformationDTO getOneFriend(Long friendId);

    List<FriendRequestDTO> getAllRequestForUser();

    Optional<List<UserDTO>> findUsersByUserName(String userName);

    User validateUser(Long userId);
}
