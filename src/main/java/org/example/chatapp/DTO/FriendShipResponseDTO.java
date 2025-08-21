package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * FriendShipResponseDTO.java
 * DTO for transferring friendship data
 * Fields:
 * - id: ID of the friendship
 * - initiatorName: Name of the user who sent the friend request
 * - recipientName: Name of the user who got the request
 * - friendsSince: A date when the friendship was created
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendShipResponseDTO {

    private Long id;
    private String initiatorName;
    private String recipientName;
    private LocalDateTime friendsSince;

}
