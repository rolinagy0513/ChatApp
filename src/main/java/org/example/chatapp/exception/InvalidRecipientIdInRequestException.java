package org.example.chatapp.exception;

public class InvalidRecipientIdInRequestException extends RuntimeException {
    public InvalidRecipientIdInRequestException(String message) {
        super(message);
    }
}
