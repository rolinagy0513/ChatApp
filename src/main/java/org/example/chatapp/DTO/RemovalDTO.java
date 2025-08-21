package org.example.chatapp.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * RemovalDTO.java
 * DTO for displaying a message when a friendship was removed
 * Fields:
 * - message: The message of the DTO
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemovalDTO {
    String message;
}
