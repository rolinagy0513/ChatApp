package org.example.chatapp.exception;

public class NotFriendsException extends RuntimeException {
    public NotFriendsException(Long id) {
        super("The user is not in the friends list with the id of: " + id);
    }
}
