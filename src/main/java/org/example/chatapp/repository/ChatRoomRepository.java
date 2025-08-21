package org.example.chatapp.repository;

import org.example.chatapp.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing ChatRoom entities.
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

    Optional<ChatRoom> findByChatId(String chatId);

}
