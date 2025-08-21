package org.example.chatapp.service;

import org.example.chatapp.model.ChatRoom;

import java.util.Optional;

/**
 * Service interface for managing chat rooms between users.
 * <p>
 * Defines operations for creating chat rooms and retrieving existing ones.
 * </p>
 * <p>
 *      For more information look at {@link org.example.chatapp.service.impl.ChatRoomService}
 * </p>
 */
public interface IChatRoomService {

    ChatRoom getOrCreateChatRoom(Long user1Id, Long user2Id);

    Optional<Long> getExistingChatroomId(Long user1Id, Long user2Id);
}
