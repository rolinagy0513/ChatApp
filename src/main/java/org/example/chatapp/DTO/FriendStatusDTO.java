package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.FriendLiveStatus;

/**
 * FriendStatusDTO.java
 * DTO for transferring status data
 * Fields:
 * - id: ID of the current user
 * - userName: The username of the current user
 * - liveStatus: A status that determines that a user is ONLINE or OFFLINE
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendStatusDTO {

    private Long id;
    private String userName;
    private FriendLiveStatus liveStatus;

}
