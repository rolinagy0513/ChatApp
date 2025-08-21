package org.example.chatapp.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * GlobalExceptionHandler.java
 * - Centralized exception handler for REST controllers.
 * - Handles various custom and database exceptions, returning
 * - meaningful HTTP responses with error details and appropriate status codes.
 * - Provides consistent error response structure for the API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Internal record to standardize API error responses.
     */
    private record ApiError(String message, String details, LocalDateTime timestamp){}

    /**
     * Handles UserNotFoundException.
     * Returns HTTP 404 with error details when a user resource is not found.
     *
     * @param ex the UserNotFoundException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFoundException(UserNotFoundException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "User resource was not found", LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles NotFriendsException.
     * Returns HTTP 404 when a friend resource is not found or users are not friends.
     *
     * @param ex the NotFriendsException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(NotFriendsException.class)
    public ResponseEntity<ApiError> handleNotFriendsException(NotFriendsException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "Friend resource was not found", LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles InvalidPasswordException.
     * Returns HTTP 400 when an invalid password is provided.
     *
     * @param ex the InvalidPasswordException thrown
     * @return ResponseEntity with ApiError and HTTP 400 status
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidPasswordException(InvalidPasswordException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "Invalid Password", LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles UserIdNotFoundException.
     * Returns HTTP 404 when a user ID is not found.
     *
     * @param ex the UserIdNotFoundException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(UserIdNotFoundException.class)
    public ResponseEntity<ApiError> handleInvalidUserIdException(UserIdNotFoundException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "User resource was not found", LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles RequestNotFoundException.
     * Returns HTTP 404 when a friend request resource is not found.
     *
     * @param ex the RequestNotFoundException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<ApiError> handleInvalidRequestIdException(RequestNotFoundException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "Request resource was not found", LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles InvalidFriendRequestException.
     * Returns HTTP 400 for invalid friend request scenarios.
     *
     * @param ex the InvalidFriendRequestException thrown
     * @return ResponseEntity with ApiError and HTTP 400 status
     */
    @ExceptionHandler(InvalidFriendRequestException.class)
    public ResponseEntity<ApiError> handleInvalidFriendRequestException(InvalidFriendRequestException ex){
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), "Invalid request", LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles DataAccessException from Spring Data.
     * Returns HTTP 500 for database operation failures with details.
     *
     * @param ex the DataAccessException thrown
     * @return ResponseEntity with ApiError and HTTP 500 status
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccessException(DataAccessException ex) {
        return new ResponseEntity<>(
                new ApiError("Database operation failed", ex.getMostSpecificCause().getMessage(), LocalDateTime.now()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Handles ChatroomCreationException
     * Returns HTTP 400 for invalid chatroom creation scenarios
     *
     * @param ex the ChatroomCreationException thrown
     * @return ResponseEntity with ApiError and HTTP 400 status
     */
    @ExceptionHandler(ChatRoomCreationException.class)
    public ResponseEntity<ApiError> handleChatroomCreationException(ChatRoomCreationException ex){
        return new ResponseEntity<>(
                new ApiError("Chatroom creation failed", ex.getMessage(),LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles handleInvalidRecipientIdInRequestException
     * Returns HTTP 400 for security issues
     *
     * @param ex the handleInvalidRecipientIdInRequestException thrown
     * @return ResponseEntity with ApiError and HTTP 400 status
     */
    @ExceptionHandler(InvalidRecipientIdInRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRecipientIdInRequestException(InvalidRecipientIdInRequestException ex){
        return new ResponseEntity<>(
                new ApiError("Security issue occurred",ex.getMessage(),LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles InvalidResponseToRequestException
     * Returns HTTP 400 for invalid response to request
     *
     * @param ex the InvalidResponseToRequestException thrown
     * @return ResponseEntity with ApiError and HTTP 400 status
     */
    @ExceptionHandler(InvalidResponseToRequestException.class)
    public ResponseEntity<ApiError> handleInvalidResponseToRequestException(InvalidResponseToRequestException ex){
        return new ResponseEntity<>(
                new ApiError("Request response sending issue", ex.getMessage(),LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles UsersAreNotFriendsException
     * Returns HTTP 404 for not founding the friendship between users
     *
     * @param ex the UsersAreNotFriendsException thrown
     * @return ResponseEntity with ApiError and HTTP 404 status
     */
    @ExceptionHandler(UsersAreNotFriendsException.class)
    public ResponseEntity<ApiError> handleUsersAreNotFriendsException(UsersAreNotFriendsException ex){
        return new ResponseEntity<>(
                new ApiError("Issue at making friendship", ex.getMessage(), LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

}
