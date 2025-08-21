package org.example.chatapp.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatapp.ENUM.FriendRequestStatus;
import org.example.chatapp.security.user.User;

import java.time.LocalDateTime;

/**
 * Model representing the FriendRequest entity.
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequest {

    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Enumerated(EnumType.STRING)
    private FriendRequestStatus status;

    private LocalDateTime sentAt;

    private boolean isAccepted;



}
