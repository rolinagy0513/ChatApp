package org.example.chatapp.DTO;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.ResponseStatus;


/**
 * RequestResponseDTO.java
 * DTO for transferring data about a response to a certain request
 * Fields:
 * - requestId: ID of the request
 * - senderId: ID of the sender
 * - status: This determines that a request was either ACCEPTED or REJECTED
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestResponseDTO {

    Long requestId;
    Long senderId;

    @Enumerated(EnumType.STRING)
    ResponseStatus status;

}
