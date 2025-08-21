package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.FriendRequestStatus;

import java.time.LocalDateTime;

/**
 * FriendRequestNotificationDTO.java
 * DTO for transferring friend request notification between two users
 * Fields:
 * - id: ID of the notification
 * - senderId: ID of the sender
 * - senderName: Name of the sender
 * - recipientId: ID of the recipient
 * - content: Content of the notification
 * - timeStamp: A date when the notification was sent
 * - status: A status about the request aka(ACCEPTED, PENDING, REJECTED)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestNotification {

    private Long id;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String content;
    private LocalDateTime timeStamp;
    private FriendRequestStatus status;

}
