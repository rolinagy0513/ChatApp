package org.example.chatapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.chatapp.exception.ChatRoomCreationException;
import org.example.chatapp.exception.UserIdNotFoundException;
import org.example.chatapp.model.ChatRoom;
import org.example.chatapp.repository.ChatRoomRepository;
import org.example.chatapp.security.user.UserRepository;
import org.example.chatapp.service.IChatRoomService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing chat room creation and retrieval.
 * Provides functionality to either fetch an existing chat room based on user IDs
 * or create a new one if it doesn't exist.
 */
@Service
@RequiredArgsConstructor
public class ChatRoomService implements IChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    /**
     * Retrieves an existing chat room ID between two users or creates a new one if it does not exist.
     * Generates a consistent chat ID by combining the user IDs and checks if a chat room already exists.
     * If not, a new chat room is created. In case of a race condition, the existing chat room is returned.
     *
     * @param user1Id ID of the first user
     * @param user2Id ID of the second user
     * @return the unique chat room ID
     * @throws UserIdNotFoundException     if either user does not exist
     * @throws ChatRoomCreationException   if chat room creation fails and no existing room is found
     */
    @Transactional
    public ChatRoom getOrCreateChatRoom(Long user1Id, Long user2Id) {

        String chatId = generateChatId(user1Id, user2Id);

        Optional<ChatRoom> existingRoom = chatRoomRepository.findByChatId(chatId);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        try {
            validateUsersExist(user1Id,user2Id);

            return chatRoomRepository.saveAndFlush(
                    ChatRoom.builder()
                            .chatId(chatId)
                            .sender(userRepository.getReferenceById(user1Id))
                            .recipient(userRepository.getReferenceById(user2Id))
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            return chatRoomRepository.findByChatId(chatId)
                    .orElseThrow(() -> new ChatRoomCreationException("Chatroom already exists"));
        }
    }

    /**
     * Retrieves the internal database ID of an existing chat room between two users, if it exists.
     *
     * @param user1Id ID of the first user
     * @param user2Id ID of the second user
     * @return an {@code Optional} containing the chat room's database ID, or empty if not found
     */
    @Transactional
    public Optional<Long> getExistingChatroomId(Long user1Id, Long user2Id) {
        String chatId = generateChatId(user1Id, user2Id);
         Optional<ChatRoom> existingRoom = chatRoomRepository.findByChatId(chatId);

        return existingRoom.map(ChatRoom::getId);

    }

    /**
     * Generates a unique, consistent chat room ID based on two user IDs.
     * <p>
     * Ensures that the order of user IDs does not affect the resulting ID.
     *
     * @param id1 the first user ID
     * @param id2 the second user ID
     * @return a string representing the chat room ID
     */
    private String generateChatId(Long id1, Long id2) {
        return Math.min(id1, id2) + "_" + Math.max(id1, id2);
    }


    /**
     * Validates the existence of both users in the database.
     *
     * @param user1Id ID of the first user
     * @param user2Id ID of the second user
     * @throws UserIdNotFoundException if either user is not found
     */
    private void validateUsersExist(Long user1Id, Long user2Id) {
        if (!userRepository.existsById(user1Id)) {
            throw new UserIdNotFoundException(user1Id);
        }
        if (!userRepository.existsById(user2Id)) {
            throw new UserIdNotFoundException(user2Id);
        }
    }

}
