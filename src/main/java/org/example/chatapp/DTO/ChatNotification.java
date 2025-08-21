package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.MessageStatus;

import java.time.LocalDateTime;

/**
 * ChatNotification.java
 * DTO for transferring chat notification data between users.
 * Fields:
 * - id: ID of the notification
 * - senderId: ID of the sender
 * - recipientId: ID of the recipient
 * - content: content of the notification
 * - timestamp: time the notification was sent
 * - messageStatus: current status of the message (e.g. SEEN, UNSEEN)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ChatNotification {
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private LocalDateTime timeStamp;
    private MessageStatus messageStatus;

}
