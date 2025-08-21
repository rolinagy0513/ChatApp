package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * FriendRequestDTO.java
 * DTO for transferring friend request between two users
 * Fields:
 * - id: ID of the request
 * - senderId: ID of the sender
 * - senderName: Name of the sender
 * - sentAt: A date when the request was sent
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private LocalDateTime sentAt;
}
