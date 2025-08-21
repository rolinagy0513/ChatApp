package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.MessageStatus;

/**
 * LastMessageDTO.java
 * DTO for displaying last messages between two users
 * Fields:
 * - senderId: ID od the sender who sent the message
 * - content: The message itself
 * - messageStatus: This determines the status of the message aka(SEEN, UNSEEN)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LastMessageDTO {

    private Long senderId;
    private String content;
    private MessageStatus messageStatus;

}
