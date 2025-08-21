package org.example.chatapp.security.user;

import lombok.RequiredArgsConstructor;
import org.example.chatapp.DTO.UserDTO;
import org.example.chatapp.exception.UserNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for user-related business logic such as
 * retrieving the current authenticated user, fetching other users,
 * and handling password changes.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;

    /**
     * Retrieves the currently authenticated user's basic information.
     *
     * @return a UserDTO containing the authenticated user's ID and full name
     * @throws UserNotFoundException if no user matches the authenticated email
     */
    public UserDTO getCurrentUser( ){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  repository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException(email));

        return UserDTO.builder()
                .id(user.getId())
                .userName(user.getFirstname() + " " + user.getLastname())
                .build();
    }

    /**
     * Retrieves the User entity corresponding to the given security Principal.
     *
     * @param principal the authenticated user's Principal
     * @return the User entity matching the principal's email
     * @throws UserNotFoundException if no user matches the principal's email or if principal is null
     */
    public User getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new UserNotFoundException("No authenticated user");
        }
        return repository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserNotFoundException(principal.getName()));
    }

    /**
     * Retrieves a list of all users excluding the one with the specified ID.
     *
     * @param excludedUserId the ID of the user to exclude
     * @return list of UserDTOs for all other users
     */
    public List<UserDTO> getAuthenticatedUsers(Long excludedUserId) {
        List<User> users = repository.findAllByIdNot(excludedUserId);

        return users.stream()
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .userName(user.getFirstname() + " " + user.getLastname())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Changes the password of the authenticated user after validating the current password
     * and confirming that the new password matches the confirmation.
     *
     * @param request the password change request containing current, new, and confirmation passwords
     * @param connectedUser the authenticated user's Principal
     * @throws IllegalStateException if the current password is incorrect or the new passwords do not match
     */
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        repository.save(user);
    }
}
