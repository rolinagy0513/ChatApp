package org.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * SearchTermDTO.java
 * DTO for transferring the search term
 * Fields:
 * - userName: The username of the searched user
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchTermDTO {

    private String userName;

}
