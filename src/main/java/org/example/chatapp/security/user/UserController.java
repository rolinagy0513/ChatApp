package org.example.chatapp.security.user;

import lombok.RequiredArgsConstructor;
import org.example.chatapp.DTO.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST controller managing user-related operations such as password changes
 * and retrieving user information.
 * <p>
 * Provides endpoints to change the authenticated user's password, fetch all users
 * except a specified one, and retrieve details of the currently authenticated user.
 * </p>
 */
@RestController
@RequestMapping("/api/Users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    /**
     * Changes the password for the currently authenticated user.
     *
     * @param request the password change request containing current, new, and confirmation passwords
     * @param connectedUser the security principal of the authenticated user
     * @return HTTP 200 OK on successful password change
     */
    @PatchMapping("/changePassword")
    public ResponseEntity<?> changePassword(
          @RequestBody ChangePasswordRequest request,
          Principal connectedUser
    ) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves all users except the user specified by the excludedUserId.
     *
     * @param excludedUserId the user ID to exclude from the results
     * @return list of UserDTO representing all other users
     */
    @GetMapping("/getAll/{excludedUserId}")
    public List<UserDTO> getALl(
            @PathVariable Long excludedUserId
    ){

        return service.getAuthenticatedUsers(excludedUserId);
    }

    /**
     * Retrieves the currently authenticated user's details.
     *
     * @return UserDTO of the authenticated user
     */
    @GetMapping("/me")
    public UserDTO getAuthenticatedUser(){
        return service.getCurrentUser();
    }

}
