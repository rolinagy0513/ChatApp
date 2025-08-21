package org.example.chatapp.exception;

public class UsersAreNotFriendsException extends RuntimeException {
    public UsersAreNotFriendsException(String message) {
        super(message);
    }
}
