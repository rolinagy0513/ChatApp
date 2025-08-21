package org.example.chatapp.service;

import org.example.chatapp.DTO.ChatMessageDTO;
import org.example.chatapp.DTO.LastMessageDTO;
import org.example.chatapp.model.ChatMessage;

import java.util.List;


/**
 * Service interface for managing chat messages between users.
 * <p>
 * Defines operations for sending, retrieving, and updating message status.
 * </p>
 * <p>
 *      For more information look at {@link org.example.chatapp.service.impl.ChatMessageService}
 * </p>
 */
public interface IChatMessageService {

    ChatMessage save(ChatMessage chatMessage);

    List<ChatMessageDTO> findChatMessages(Long user1Id, Long user2Id);

    LastMessageDTO findLastMessage(Long user1Id, Long user2Id);

    void markMessagesAsSeen(Long senderId, Long recipientId);
}
