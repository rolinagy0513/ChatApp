package org.example.chatapp.exception;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(Long id) {
        super("Request is not found with the id of: " + id);
    }
}
