package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserDTO.java
 * DTO for transferring data about a certain user
 * Fields:
 * - id: ID of the user
 * - userName: Username of the user
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String userName;

}
