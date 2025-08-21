package org.example.chatapp.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("The user is not found with the id of : " + email);
    }
}
