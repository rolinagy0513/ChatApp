package org.example.chatapp.repository;

import org.example.chatapp.model.ChatMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing ChatMessage entities.
 * Provides optimized methods to retrieve messages with relationships loaded in single queries.
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Retrieves all chat messages in a chat room with sender and recipient loaded,
     * ordered chronologically. Uses eager loading to avoid N+1 queries.
     * @param chatId the unique identifier of the chat room
     * @return list of chat messages with users loaded
     */
    @EntityGraph(attributePaths = {"sender", "recipient"})
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatRoom.chatId = :chatId " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findMessagesWithUsersByChatId(@Param("chatId") String chatId);

    /**
     * Retrieves the most recent message in a chat room with sender and recipient loaded.
     * Uses eager loading to avoid additional queries for relationships.
     * @param chatRoomId the database ID of the chat room
     * @return optional containing the last message with users loaded if found
     */
    @EntityGraph(attributePaths = {"sender", "recipient"})
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatRoom.id = :chatRoomId " +
            "ORDER BY m.timestamp DESC " +
            "LIMIT 1")
    Optional<ChatMessage> findLastMessageWithUsersByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * Marks all messages with status {@code UNSEEN} as {@code SEEN} in the specified chat room,
     * where the sender matches the given user ID.
     *
     * @param chatRoomId the ID of the chat room
     * @param senderId   the ID of the sender whose messages should be updated
     */

    @Modifying
    @Query("UPDATE ChatMessage m SET m.messageStatus = 'SEEN' " +
            "WHERE m.chatRoom.id = :chatRoomId " +
            "AND m.sender.id = :senderId " +
            "AND m.messageStatus = 'UNSEEN'")
    void markMessagesSeen(@Param("chatRoomId") Long chatRoomId,
                          @Param("senderId") Long senderId);

}

