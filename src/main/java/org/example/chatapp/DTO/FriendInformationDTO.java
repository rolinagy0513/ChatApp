package org.example.chatapp.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.FriendLiveStatus;

import java.time.LocalDateTime;


/**
 * FriendInformationDTO.java
 * DTO for displaying data of a certain user.
 * Fields:
 * - userName: Username of the user(firstname + lastname combined)
 * - firstname: First name of the user
 * - lastname: Last name of the user
 * - email: Email of the user
 * - friendsSince: A date when the friendship was created between two users
 * - liveStatus: A status that determines that a user is ONLINE or OFFLINE
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendInformationDTO {

    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime friendsSince;
    private FriendLiveStatus liveStatus;

}
