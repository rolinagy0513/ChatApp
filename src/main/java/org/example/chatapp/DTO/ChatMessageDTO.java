package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.MessageStatus;

import java.time.LocalDateTime;

/**
 * ChatMessageDTO.java
 * DTO for transferring chat message data between users.
 * Fields:
 * - senderId: ID of the user sending the message
 * - recipientId: ID of the message recipient
 * - content: text content of the message
 * - timestamp: time the message was sent
 * - messageStatus: current status of the message (e.g. SEEN, UNSEEN)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDTO {

    private Long senderId;
    private Long recipientId;
    private String content;
    private LocalDateTime timestamp;
    private MessageStatus messageStatus;

}
