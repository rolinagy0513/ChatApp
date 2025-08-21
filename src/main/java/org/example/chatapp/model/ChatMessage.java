package org.example.chatapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.MessageStatus;
import org.example.chatapp.security.user.User;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Model representing the ChatMessage entity.
 */
@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Builder

@NamedEntityGraph(name = "ChatMessage.withUsers",
        attributeNodes = {
                @NamedAttributeNode("sender"),
                @NamedAttributeNode("recipient")
        }
)

public class ChatMessage {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    private String content;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus;

}
