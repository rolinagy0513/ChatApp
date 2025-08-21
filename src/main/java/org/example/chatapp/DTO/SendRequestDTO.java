package org.example.chatapp.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SendRequestDTO.java
 * DTO for sending a friend request
 * Fields:
 * - recipientId: The id of the recipient
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendRequestDTO {

    Long recipientId;

}
