package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.FriendLiveStatus;

import java.time.LocalDateTime;

/**
 * FriendShipDTO.java
 * DTO for displaying information about a friend
 * Fields:
 * - id: ID of the friendship
 * - username: Username of the friend
 * - friendsSince: A date when the friendship was created
 * - liveStatus: A status that determines that a user is ONLINE or OFFLINE
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendShipDTO {

    private Long id;
    private String userName;
    private LocalDateTime friendsSince;
    private FriendLiveStatus liveStatus;

}
