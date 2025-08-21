package org.example.chatapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.chatapp.DTO.ChatMessageDTO;
import org.example.chatapp.DTO.LastMessageDTO;
import org.example.chatapp.model.ChatMessage;
import org.example.chatapp.model.ChatRoom;
import org.example.chatapp.repository.ChatMessageRepository;
import org.example.chatapp.service.IChatMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing chat messages between users.
 * <p>
 * Responsible for:
 * <ul>
 *     <li>Saving new messages to the database</li>
 *     <li>Retrieving chat history and the last exchanged message between users</li>
 *     <li>Updating message statuses (e.g., marking messages as SEEN)</li>
 * </ul>
 */

@Service
@RequiredArgsConstructor
public class ChatMessageService implements IChatMessageService {

    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;

    /**
     * Persists a new chat message to the database.
     * <p>
     * Ensures a chat room exists between the sender and recipient—creates one if necessary—
     * then associates the message with that room and sets the current timestamp.
     *
     * @param chatMessage the message to be saved
     * @return the persisted {@link ChatMessage} instance
     */
    @Transactional
    public ChatMessage save(ChatMessage chatMessage) {

        Long user1Id = chatMessage.getSender().getId();
        Long user2Id = chatMessage.getRecipient().getId();

        ChatRoom chatRoom = chatRoomService.getOrCreateChatRoom(user1Id,user2Id);

        chatMessage.setChatRoom(chatRoom);
        chatMessage.setTimestamp(LocalDateTime.now());
        return repository.save(chatMessage);

    }

    /**
     * Retrieves the full chat history between two users.
     * <p>
     * Ensures a chat room exists between the users—creates one if necessary—
     * and fetches messages ordered by timestamp. Uses an optimized query with `@EntityGraph`
     * to eagerly load sender and recipient user data.
     *
     * @param user1Id the ID of the first user
     * @param user2Id the ID of the second user
     * @return a list of {@link ChatMessageDTO} representing the chat history
     */

    @Transactional
    public List<ChatMessageDTO> findChatMessages(Long user1Id, Long user2Id) {
        ChatRoom chatRoom = chatRoomService.getOrCreateChatRoom(user1Id, user2Id);
        String chatId = chatRoom.getChatId();
        List<ChatMessage> messages = repository.findMessagesWithUsersByChatId(chatId);

        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the most recent message exchanged between two users, regardless of direction.
     * <p>
     * Returns {@code null} if no chat room exists or if no messages have been exchanged.
     *
     * @param user1Id the ID of the first user
     * @param user2Id the ID of the second user
     * @return the latest message as a {@link LastMessageDTO}, or {@code null} if not found
     */
    @Transactional(readOnly = true)
    public LastMessageDTO findLastMessage(Long user1Id, Long user2Id) {
        Optional<Long> optionalChatRoomDbId = chatRoomService.getExistingChatroomId(user1Id, user2Id);

        if (optionalChatRoomDbId.isEmpty()) {
            return null;
        }

        Long chatRoomDbId = optionalChatRoomDbId.get();
        Optional<ChatMessage> lastMessageOpt = repository.findLastMessageWithUsersByChatRoomId(chatRoomDbId);

        if (lastMessageOpt.isEmpty()) {
            return null;
        }

        ChatMessage lastMessage = lastMessageOpt.get();

        return LastMessageDTO.builder()
                .senderId(lastMessage.getSender().getId())
                .content(lastMessage.getContent())
                .messageStatus(lastMessage.getMessageStatus())
                .build();
    }

    /**
     * Marks all messages from a specific sender as {@code SEEN} in an existing chat room.
     * <p>
     * This method is typically used when a recipient opens a conversation,
     * allowing the UI to reflect updated message status.
     *
     * @param senderId    the ID of the user who sent the messages
     * @param recipientId the ID of the recipient (viewer of the messages)
     */
    @Transactional
    public void markMessagesAsSeen(Long senderId, Long recipientId) {
        chatRoomService.getExistingChatroomId(senderId, recipientId)
                .ifPresent(chatRoomDbId ->
                        repository.markMessagesSeen(chatRoomDbId, senderId)
                );
    }

    /**
     * Converts a {@link ChatMessage} entity to a {@link ChatMessageDTO}.
     *
     * @param message the message entity to convert
     * @return the DTO representation of the message
     */
    private ChatMessageDTO convertToDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .senderId(message.getSender().getId())
                .recipientId(message.getRecipient().getId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .messageStatus(message.getMessageStatus())
                .build();
    }

}
