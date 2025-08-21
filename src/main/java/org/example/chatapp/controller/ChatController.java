package org.example.chatapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chatapp.DTO.ChatMessageDTO;
import org.example.chatapp.DTO.ChatNotification;
import org.example.chatapp.DTO.LastMessageDTO;
import org.example.chatapp.ENUM.MessageStatus;
import org.example.chatapp.model.ChatMessage;
import org.example.chatapp.security.user.User;
import org.example.chatapp.security.user.UserService;
import org.example.chatapp.service.impl.ChatMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * ChatController.java
 * Handles WebSocket messaging and REST endpoints related to chat functionality.
 * Provides real-time communication, message retrieval, and message status updates.
 */

@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/Chat")
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final UserService userService;

    /**
     * Receives and processes incoming chat messages via WebSocket.
     * Stores the message and sends notifications to both sender and recipient.
     *
     * @param chatMessage the incoming chat message payload
     * @param principal   the authenticated user sending the message
     */
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage, Principal principal) {

        User sender = userService.getUserFromPrincipal(principal);

        chatMessage.setSender(sender);
        chatMessage.setMessageStatus(MessageStatus.UNSEEN);

        if (chatMessage.getRecipient() == null || chatMessage.getRecipient().getId() == null) {
            log.error("ERROR: Recipient is null or has null ID!");
            return;
        }

        ChatMessage savedMsg = chatMessageService.save(chatMessage);

        String recipientUserId = savedMsg.getRecipient().getId().toString();
        String senderUserId = savedMsg.getSender().getId().toString();

        ChatNotification notification = ChatNotification.builder()
                .id(savedMsg.getId())
                .senderId(savedMsg.getSender().getId())
                .recipientId(savedMsg.getRecipient().getId())
                .content(savedMsg.getContent())
                .timeStamp(savedMsg.getTimestamp())
                .messageStatus(savedMsg.getMessageStatus())
                .build();

        messagingTemplate.convertAndSendToUser(
                recipientUserId, "/queue/messages", notification
        );

        messagingTemplate.convertAndSendToUser(
                senderUserId, "/queue/messages", notification
        );
    }

    /**
     * Retrieves the full conversation history between two users.
     *
     * @param user1Id ID of the first user
     * @param user2Id ID of the second user
     * @return list of chat messages
     */
    @GetMapping("/messages/{user1Id}/{user2Id}")
    public List<ChatMessageDTO> findChatMessages(
            @PathVariable Long user1Id,
            @PathVariable Long user2Id) {

        return chatMessageService.findChatMessages(user1Id,user2Id);
    }


    /**
     * Retrieves the most recent message exchanged between two users.
     *
     * @param user1Id ID of the first user
     * @param user2Id ID of the second user
     * @return last message as a DTO
     */
    @GetMapping("/lastMessage/{user1Id}/{user2Id}")
    public LastMessageDTO findLastMessage(
            @PathVariable Long user1Id,
            @PathVariable Long user2Id
    ){
        return chatMessageService.findLastMessage(user1Id, user2Id);
    }

    /**
     * Marks all messages from a specific sender as seen by the recipient.
     *
     * @param senderId    ID of the message sender
     * @param recipientId ID of the message recipient
     * @param principal   the currently authenticated user
     * @return success message
     */
    @PostMapping("/messages/markAsSeen/{senderId}/{recipientId}")
    public ResponseEntity<?> markMessagesAsSeen(
            @PathVariable Long senderId,
            @PathVariable Long recipientId,
            Principal principal) {

        User currentUser = userService.getUserFromPrincipal(principal);

        if (!currentUser.getId().equals(recipientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        chatMessageService.markMessagesAsSeen(senderId, recipientId);

        return ResponseEntity.ok().body(Map.of("message", "Messages marked as seen successfully"));
    }

}