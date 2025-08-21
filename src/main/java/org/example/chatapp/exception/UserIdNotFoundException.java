package org.example.chatapp.exception;

public class UserIdNotFoundException extends RuntimeException {
    public UserIdNotFoundException(Long id) {
        super("The user is not found with the id of: " + id);
    }
}
