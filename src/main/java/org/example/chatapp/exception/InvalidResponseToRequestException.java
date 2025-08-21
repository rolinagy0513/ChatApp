package org.example.chatapp.exception;

public class InvalidResponseToRequestException extends RuntimeException {
    public InvalidResponseToRequestException(String message) {
        super(message);
    }
}
