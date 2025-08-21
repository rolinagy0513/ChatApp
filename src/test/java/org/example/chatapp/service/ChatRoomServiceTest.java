package org.example.chatapp.service;

import org.example.chatapp.exception.UserIdNotFoundException;
import org.example.chatapp.model.ChatRoom;
import org.example.chatapp.repository.ChatRoomRepository;
import org.example.chatapp.security.user.User;
import org.example.chatapp.security.user.UserRepository;
import org.example.chatapp.service.impl.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ChatRoomService}.
 * <p>
 * This class verifies the behavior of chat room creation and retrieval logic,
 * ensuring that the service correctly handles:
 * <ul>
 *   <li>Returning existing chat rooms</li>
 *   <li>Creating new chat rooms when none exist</li>
 *   <li>Validating user existence before creating a room</li>
 *   <li>Handling race conditions and constraint violations</li>
 *   <li>Fetching chat room IDs safely</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatRoomService service;

    private User user1;
    private ChatRoom chatRoom;

    @BeforeEach
    void setup() {
        user1 = User.builder()
                .id(1L)
                .userName("First user")
                .email("first@user.com")
                .firstname("First")
                .lastname("user")
                .sentChatRooms(new ArrayList<>())
                .receivedChatRooms(new ArrayList<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .userName("Second user")
                .email("second@user.com")
                .firstname("Second")
                .lastname("user")
                .sentChatRooms(new ArrayList<>())
                .receivedChatRooms(new ArrayList<>())
                .build();

        chatRoom = ChatRoom.builder()
                .id(1L)
                .chatId("1_2")
                .sender(user1)
                .recipient(user2)
                .build();

        user1.getSentChatRooms().add(chatRoom);
        user2.getReceivedChatRooms().add(chatRoom);
    }

    /**
     * Verifies that when a chat room already exists, the service
     * returns the existing room instead of creating a new one.
     */
    @Test
    void getOrCreateChatRoom_WithExistingChatRoomId_ShouldReturnExistingRoom() {
        when(chatRoomRepository.findByChatId("1_2")).thenReturn(Optional.of(chatRoom));

        ChatRoom result = service.getOrCreateChatRoom(1L, 2L);

        assertNotNull(result);
        assertEquals("1_2", result.getChatId());
        assertEquals(1L, result.getSender().getId());
        assertEquals(2L, result.getRecipient().getId());

        verify(chatRoomRepository).findByChatId("1_2");
    }

    /**
     * Verifies that when no chat room exists and both users exist,
     * the service creates and returns a new chat room.
     */
    @Test
    void getOrCreateChatRoom_WithNonExistingChatRoomId_ShouldCreateAndReturnNew() {
        User testUser = User.builder()
                .id(3L)
                .userName("Test user")
                .email("test@user.com")
                .firstname("Test")
                .lastname("User")
                .sentChatRooms(new ArrayList<>())
                .receivedChatRooms(new ArrayList<>())
                .build();

        ChatRoom newChatroom = ChatRoom.builder()
                .id(2L)
                .chatId("1_3")
                .sender(user1)
                .recipient(testUser)
                .build();

        user1.getSentChatRooms().add(newChatroom);
        testUser.getReceivedChatRooms().add(newChatroom);

        when(chatRoomRepository.findByChatId("1_3")).thenReturn(Optional.empty());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(3L)).thenReturn(true);
        when(chatRoomRepository.saveAndFlush(any(ChatRoom.class))).thenReturn(newChatroom);

        ChatRoom result = service.getOrCreateChatRoom(1L, 3L);

        assertNotNull(result);
        assertEquals("1_3", result.getChatId());
        assertEquals(1L, result.getSender().getId());
        assertEquals(3L, result.getRecipient().getId());

        verify(chatRoomRepository).findByChatId("1_3");
        verify(userRepository).existsById(1L);
        verify(userRepository).existsById(3L);
        verify(chatRoomRepository).saveAndFlush(any(ChatRoom.class));
    }

    /**
     * Verifies that if either user does not exist, the service
     * throws {@link UserIdNotFoundException} and does not create a chat room.
     */
    @Test
    void getOrCreateChatRoom_WithNonExistingUser_ShouldThrowUserIdNotFoundException() {
        Long validUserId = 1L;
        Long invalidUserId = 3L;

        when(chatRoomRepository.findByChatId("1_3")).thenReturn(Optional.empty());
        when(userRepository.existsById(validUserId)).thenReturn(true);
        when(userRepository.existsById(invalidUserId)).thenReturn(false);

        assertThrows(UserIdNotFoundException.class, () -> {
            service.getOrCreateChatRoom(validUserId, invalidUserId);
        });

        verify(chatRoomRepository).findByChatId("1_3");
        verify(userRepository).existsById(validUserId);
        verify(userRepository).existsById(invalidUserId);
        verify(chatRoomRepository, never()).saveAndFlush(any(ChatRoom.class));
    }

    /**
     * Verifies that if a {@link DataIntegrityViolationException} occurs during creation,
     * the service retrieves and returns the existing chat room created concurrently.
     */
    @Test
    void getOrCreateChatRoom_WhenDataIntegrityViolationOccurs_ShouldReturnExistingRoom() {
        Long user1Id = 1L;
        Long user2Id = 3L;
        String chatId = "1_3";

        ChatRoom existingRoom = ChatRoom.builder()
                .id(2L)
                .chatId(chatId)
                .sender(user1)
                .recipient(User.builder().id(3L).build())
                .build();

        when(chatRoomRepository.findByChatId(chatId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingRoom));
        when(userRepository.existsById(user1Id)).thenReturn(true);
        when(userRepository.existsById(user2Id)).thenReturn(true);
        when(chatRoomRepository.saveAndFlush(any(ChatRoom.class)))
                .thenThrow(DataIntegrityViolationException.class);

        ChatRoom result = service.getOrCreateChatRoom(user1Id, user2Id);

        assertNotNull(result);
        assertEquals(existingRoom.getId(), result.getId());
        assertEquals(chatId, result.getChatId());

        verify(chatRoomRepository, times(2)).findByChatId(chatId);
        verify(chatRoomRepository).saveAndFlush(any(ChatRoom.class));
    }

    /**
     * Verifies that when a chat room exists for given user IDs,
     * {@link ChatRoomService#getExistingChatroomId(Long, Long)} returns its ID.
     */
    @Test
    void getExistingChatRoomId_WithValidData_ShouldReturnChatRoomId() {
        when(chatRoomRepository.findByChatId("1_2")).thenReturn(Optional.of(chatRoom));

        Optional<Long> result = service.getExistingChatroomId(1L, 2L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get());

        verify(chatRoomRepository).findByChatId("1_2");
    }

    /**
     * Verifies that when no chat room exists for given user IDs,
     * {@link ChatRoomService#getExistingChatroomId(Long, Long)} returns {@link Optional#empty()}.
     */
    @Test
    void getExistingChatRoomId_WithInvalidData_ShouldReturnOptionalEmpty() {
        when(chatRoomRepository.findByChatId("1_3")).thenReturn(Optional.empty());

        Optional<Long> result = service.getExistingChatroomId(1L, 3L);

        assertTrue(result.isEmpty());

        verify(chatRoomRepository).findByChatId("1_3");
    }
}
