package org.example.chatapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.security.user.User;

import java.time.LocalDateTime;

/**
 * Model representing the FriendShip entity.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder

@NamedEntityGraph(name = "Friendship.withUsers",
        attributeNodes = {
                @NamedAttributeNode("user1"),
                @NamedAttributeNode("user2")
        }
)

public class FriendShip {

    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
