package org.example.chatapp.service;

import org.example.chatapp.DTO.ChatMessageDTO;
import org.example.chatapp.DTO.LastMessageDTO;
import org.example.chatapp.exception.UserIdNotFoundException;
import org.example.chatapp.model.ChatMessage;
import org.example.chatapp.model.ChatRoom;
import org.example.chatapp.repository.ChatMessageRepository;
import org.example.chatapp.security.user.User;
import org.example.chatapp.service.impl.ChatMessageService;
import org.example.chatapp.service.impl.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ChatMessageService}.
 * <p>
 * This class verifies the behavior of chat message management logic,
 * ensuring that the service correctly handles:
 * <ul>
 *   <li>Saving new chat messages with proper room association</li>
 *   <li>Retrieving chat histories between users</li>
 *   <li>Fetching the last message in a conversation</li>
 *   <li>Marking messages as seen by recipients</li>
 *   <li>Proper error handling for invalid user IDs</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatRoomService chatRoomService;

    @InjectMocks
    private ChatMessageService service;

    private User user1;
    private User user2;
    private ChatRoom chatRoom;

    @BeforeEach
    void setup(){
        user1 = User.builder()
                .id(1L)
                .userName("First user")
                .email("first@user.com")
                .firstname("First")
                .lastname("user")
                .sentChatRooms(new ArrayList<>())
                .receivedChatRooms(new ArrayList<>())
                .build();

         user2 = User.builder()
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
     * Verifies that when saving a valid chat message:
     * <ul>
     *   <li>The chat room is retrieved or created</li>
     *   <li>The message is associated with the chat room</li>
     *   <li>A timestamp is automatically set</li>
     *   <li>The message is persisted to the repository</li>
     * </ul>
     */
    @Test
    void save_WithValidData_ShouldSaveChatMessage() {

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(user1)
                .recipient(user2)
                .content("Hello")
                .build();

        when(chatRoomService.getOrCreateChatRoom(1L, 2L))
                .thenReturn(chatRoom);

        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage result = service.save(chatMessage);

        assertNotNull(result);
        assertEquals(chatRoom, result.getChatRoom());
        assertNotNull(result.getTimestamp());
        assertTrue(result.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));

        verify(chatRoomService).getOrCreateChatRoom(1L, 2L);
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    /**
     * Verifies that when attempting to save a message with non-existent users:
     * <ul>
     *   <li>{@link UserIdNotFoundException} is thrown</li>
     *   <li>No message is persisted to the repository</li>
     *   <li>The chat room service is properly consulted</li>
     * </ul>
     */
    @Test
    void save_WhenUserDoesNotExist_ShouldThrowUserIdNotFoundException() {
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(user1)
                .recipient(user2)
                .content("Hello")
                .build();

        when(chatRoomService.getOrCreateChatRoom(1L, 2L))
                .thenThrow(new UserIdNotFoundException(2L));

        assertThrows(UserIdNotFoundException.class, () -> {
            service.save(chatMessage);
        });

        verify(chatRoomService).getOrCreateChatRoom(1L, 2L);
        verify(chatMessageRepository,never()).save(any(ChatMessage.class));
    }

    /**
     * Verifies that when retrieving chat messages between existing users:
     * <ul>
     *   <li>The chat room is properly retrieved or created</li>
     *   <li>Messages are converted to DTO format</li>
     *   <li>The repository query includes user information</li>
     *   <li>All messages are returned in the response</li>
     * </ul>
     */
    @Test
    void findChatMessages_WithExistingChatRoom_ShouldReturnMessages() {
        when(chatRoomService.getOrCreateChatRoom(1L, 2L))
                .thenReturn(chatRoom);

        ChatMessage message1 = ChatMessage.builder()
                .sender(user1)
                .recipient(user2)
                .content("Hello")
                .timestamp(LocalDateTime.now())
                .build();

        when(chatMessageRepository.findMessagesWithUsersByChatId("1_2"))
                .thenReturn(List.of(message1));

        List<ChatMessageDTO> results = service.findChatMessages(1L, 2L);

        assertEquals(1, results.size());
        assertEquals("Hello", results.get(0).getContent());
        verify(chatRoomService).getOrCreateChatRoom(1L, 2L);
        verify(chatMessageRepository).findMessagesWithUsersByChatId("1_2");
    }

    /**
     * Verifies that when retrieving the last message from an existing chat:
     * <ul>
     *   <li>The chat room ID is properly looked up</li>
     *   <li>The most recent message is identified</li>
     *   <li>Sender information is included in the DTO</li>
     *   <li>Message content and status are preserved</li>
     * </ul>
     */
    @Test
    void findLastMessage_WithExistingChatAndMessages_ShouldReturnDTO() {
        // Arrange
        when(chatRoomService.getExistingChatroomId(1L, 2L))
                .thenReturn(Optional.of(1L));

        ChatMessage lastMessage = ChatMessage.builder()
                .sender(user1)
                .recipient(user2)
                .content("Last message")
                .build();

        when(chatMessageRepository.findLastMessageWithUsersByChatRoomId(1L))
                .thenReturn(Optional.of(lastMessage));

        LastMessageDTO result = service.findLastMessage(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals("Last message", result.getContent());
        assertEquals(1L, result.getSenderId());
        verify(chatRoomService).getExistingChatroomId(1L, 2L);
        verify(chatMessageRepository).findLastMessageWithUsersByChatRoomId(1L);
    }

    /**
     * Verifies that when retrieving the last message from an empty chat:
     * <ul>
     *   <li>The chat room ID is properly looked up</li>
     *   <li>No messages are found in the repository</li>
     *   <li>{@code null} is returned as specified</li>
     *   <li>The message repository is properly consulted</li>
     * </ul>
     */
    @Test
    void findLastMessage_WithExistingChatButNoMessages_ShouldReturnNull() {
        when(chatRoomService.getExistingChatroomId(1L, 2L))
                .thenReturn(Optional.of(1L));

        when(chatMessageRepository.findLastMessageWithUsersByChatRoomId(1L))
                .thenReturn(Optional.empty());

        LastMessageDTO result = service.findLastMessage(1L, 2L);

        assertNull(result);
        verify(chatRoomService).getExistingChatroomId(1L, 2L);
        verify(chatMessageRepository).findLastMessageWithUsersByChatRoomId(1L);
    }

    /**
     * Verifies that when attempting to get the last message from non-existent chat:
     * <ul>
     *   <li>No chat room ID is found</li>
     *   <li>{@code null} is returned without repository access</li>
     *   <li>The message repository is not consulted</li>
     * </ul>
     */
    @Test
    void findLastMessage_WithNoChatRoom_ShouldReturnNull() {
        when(chatRoomService.getExistingChatroomId(1L, 2L))
                .thenReturn(Optional.empty());

        LastMessageDTO result = service.findLastMessage(1L, 2L);

        assertNull(result);
        verify(chatRoomService).getExistingChatroomId(1L, 2L);
        verify(chatMessageRepository, never()).findLastMessageWithUsersByChatRoomId(anyLong());
    }

    /**
     * Verifies that when marking messages as seen in an existing chat:
     * <ul>
     *   <li>The chat room ID is properly looked up</li>
     *   <li>The repository update method is invoked</li>
     *   <li>Only messages from the specified sender are updated</li>
     * </ul>
     */
    @Test
    void markMessagesAsSeen_WithExistingChatRoom_ShouldUpdateMessages() {
        when(chatRoomService.getExistingChatroomId(1L, 2L))
                .thenReturn(Optional.of(1L));

        service.markMessagesAsSeen(1L, 2L);

        verify(chatRoomService).getExistingChatroomId(1L, 2L);
        verify(chatMessageRepository).markMessagesSeen(1L, 1L);
    }

    /**
     * Verifies that when attempting to mark messages in non-existent chat:
     * <ul>
     *   <li>No chat room ID is found</li>
     *   <li>No repository updates are performed</li>
     *   <li>The operation completes without errors</li>
     * </ul>
     */
    @Test
    void markMessagesAsSeen_WithNoChatRoom_ShouldDoNothing() {
        when(chatRoomService.getExistingChatroomId(1L, 2L))
                .thenReturn(Optional.empty());

        service.markMessagesAsSeen(1L, 2L);

        verify(chatRoomService).getExistingChatroomId(1L, 2L);
        verify(chatMessageRepository, never()).markMessagesSeen(anyLong(), anyLong());
    }

}
