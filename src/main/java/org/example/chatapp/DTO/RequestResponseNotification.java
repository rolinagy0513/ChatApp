package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.ResponseStatus;

import java.time.LocalDateTime;


/**
 * RequestResponseNotification.java
 * DTO for displaying a notification about a response to a certain request
 * Fields:
 * - senderId: ID od the sender
 * - recipientId: ID of the recipient
 * - senderName: The name of the sender
 * - content: The content of the DTO
 * - friendsSince: The date when the friendship was created
 * - status: The status of the response which is either ACCEPTED or REJECTED
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestResponseNotification {
    Long senderId;
    Long recipientId;
    String senderName;
    String content;
    LocalDateTime friendsSince;
    ResponseStatus status;
}
