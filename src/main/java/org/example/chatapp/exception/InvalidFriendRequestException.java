package org.example.chatapp.exception;

public class InvalidFriendRequestException extends RuntimeException {
    public InvalidFriendRequestException(String message) {
        super(message);
    }
}
