package org.example.chatapp.repository;

import org.example.chatapp.ENUM.FriendRequestStatus;
import org.example.chatapp.model.FriendRequest;
import org.example.chatapp.security.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for managing FriendRequest entities.
 * Provides methods to check existence and retrieve pending friend requests.
 */
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    /**
     * Checks if a friend request exists between the given sender and recipient with the specified status.
     *
     * @param sender the user who sent the friend request
     * @param recipient the user who received the friend request
     * @param status the status of the friend request (e.g., PENDING, ACCEPTED)
     * @return true if such a friend request exists, false otherwise
     */
    boolean existsBySenderAndRecipientAndStatus(User sender, User recipient, FriendRequestStatus status);

    /**
     * Retrieves all pending friend requests for the specified recipient.
     *
     * @param recipientId the ID of the user receiving friend requests
     * @return list of pending friend requests for the recipient
     */
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.recipient.id = :recipientId AND fr.status = org.example.chatapp.ENUM.FriendRequestStatus.PENDING")
    List<FriendRequest> findPendingRequestsByRecipientId(@Param("recipientId") Long recipientId);



}
