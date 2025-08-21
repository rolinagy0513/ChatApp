package org.example.chatapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chatapp.DTO.*;
import org.example.chatapp.ENUM.ResponseStatus;
import org.example.chatapp.service.impl.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * FriendController.java
 * Handles friend-related functionality including sending requests,
 * responding to requests, searching users, and managing friendships.
 */

@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/Friend")
@Slf4j
public class FriendController {

    private final FriendService friendService;

    /**
     * Sends a friend request from the authenticated user to the recipient.
     * Triggered via WebSocket at endpoint: /app/friend-request
     *
     * @param requestDTO contains recipient ID
     * @param principal  the authenticated user
     */
    @MessageMapping("/friend-request")
    public void processRequest(@Payload SendRequestDTO requestDTO, Principal principal) {

        Long recipientId = requestDTO.getRecipientId();

        friendService.sendFriendRequest(recipientId,principal);

    }

    /**
     * Processes a response (accept/decline) to a received friend request.
     * Triggered via WebSocket at endpoint: /app/friend-request-response
     *
     * @param responseDTO contains request ID, sender ID, and response status
     * @param principal   the authenticated user responding
     */
    @MessageMapping("/friend-request-response")
    public void processRequestResponse(@Payload RequestResponseDTO responseDTO, Principal principal){

        Long requestId = responseDTO.getRequestId();;
        ResponseStatus response = responseDTO.getStatus();

        friendService.sendRequestResponse(requestId, response, principal);

    }

    /**
     * Retrieves the current user's list of accepted friends.
     *
     * @param principal the authenticated user
     * @return list of FriendShipDTOs
     */
    @GetMapping("/getFriends")
    public CompletableFuture<ResponseEntity<List<FriendShipDTO>>> getAllFriends(Principal principal){
        return friendService.getAllFriendships(principal)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Retrieves profile information for a specific friend by ID.
     *
     * @param friendId the friend's user ID
     * @return detailed friend information
     */
    @GetMapping("/getOne/{friendId}")
    public FriendInformationDTO getOneFriendInfo(
            @PathVariable Long friendId
    ){
        return friendService.getOneFriend(friendId);
    }

    /**
     * Retrieves all PENDING friend requests for the authenticated user.
     *
     * @return list of friend request DTOs
     */
    @GetMapping("/getRequests")
    public List<FriendRequestDTO> getAllRequestsForUser(){
        return friendService.getAllRequestForUser();
    }


    /**
     * Searches for users by a username keyword.
     *
     * @param searchTermDTO contains the search term (username)
     * @return list of matching users
     */
    @PostMapping("/searchUser")
    public ResponseEntity<List<UserDTO>> findUsers(
            @RequestBody SearchTermDTO searchTermDTO
    ) {
        List<UserDTO> results = friendService.findUsersByUserName(searchTermDTO.getUserName())
                .orElse(Collections.emptyList());
        return ResponseEntity.ok(results);
    }

    /**
     * Removes a friend from the authenticated user's friend list.
     *
     * @param otherUserId ID of the user to remove from the friend list
     * @return confirmation message
     */
    @DeleteMapping("/removeFriend/{otherUserId}")
    public ResponseEntity<Map<String, Object>> removeFriend(
            @PathVariable Long otherUserId
    ) {
        friendService.removeFriendship(otherUserId);
        return ResponseEntity.ok()
                .body(Map.of(
                        "success", true,
                        "message", "User successfully removed from friends list"
                ));
    }

}
