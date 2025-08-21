package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserResponseDTO.java
 * DTO for transferring data about a certain user
 * Fields:
 * - id: ID of the user
 * - userName: Username of the user
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String userName;
}
