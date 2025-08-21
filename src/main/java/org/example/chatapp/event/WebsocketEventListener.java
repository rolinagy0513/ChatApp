package org.example.chatapp.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chatapp.DTO.FriendStatusDTO;
import org.example.chatapp.ENUM.FriendLiveStatus;
import org.example.chatapp.exception.UserNotFoundException;
import org.example.chatapp.repository.FriendShipRepository;
import org.example.chatapp.security.user.User;
import org.example.chatapp.security.user.UserRepository;
import org.example.chatapp.service.impl.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

/**
 * WebsocketEventListener.java
 * - Handles WebSocket connection events to manage user presence status.
 * - Listens for session connect and disconnect events to update users' online/offline status
 * - and notifies their friends about these changes via WebSocket messaging asynchronously.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsocketEventListener {

    private final PresenceService presenceService;
    private final UserRepository userRepository;
    private final FriendShipRepository friendShipRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles a new WebSocket session connection event.
     * Sets the user status to ONLINE and notifies all friends.
     *
     * @param event the WebSocket session connect event containing session details
     */
    @EventListener
    @Async("asyncExecutor")
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String email = sha.getUser() != null ? sha.getUser().getName() : null;

        if (email != null) {
            presenceService.setOnline(email);
            notifyFriendsAboutStatus(email, FriendLiveStatus.ONLINE);
        }
    }

    /**
     * Handles a WebSocket session disconnect event.
     * Sets the user status to OFFLINE and notifies all friends.
     *
     * @param event the WebSocket session disconnect event containing session details
     */
    @EventListener
    @Async("asyncExecutor")
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String email = sha.getUser() != null ? sha.getUser().getName() : null;

        if (email != null) {
            presenceService.setOffline(email);
            notifyFriendsAboutStatus(email, FriendLiveStatus.OFFLINE);
        }
    }

    /**
     * Notifies all friends of a user about their current live status.
     * Sends a FriendStatusDTO message to each friend via WebSocket messaging.
     *
     * @param userEmail the email of the user whose status changed
     * @param liveStatus the new live status of the user (ONLINE or OFFLINE)
     */
    private void notifyFriendsAboutStatus(String userEmail, FriendLiveStatus liveStatus) {
        try {
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

            List<Long> friendIds = friendShipRepository.findFriendIdsByUserId(currentUser.getId());

            if (friendIds.isEmpty()) {
                return;
            }

            FriendStatusDTO statusDTO = FriendStatusDTO.builder()
                    .id(currentUser.getId())
                    .userName(currentUser.getName())
                    .liveStatus(liveStatus)
                    .build();

            for (Long friendId : friendIds) {
                messagingTemplate.convertAndSendToUser(
                        friendId.toString(),
                        "/queue/isOnline",
                        statusDTO
                );
            }

        } catch (UserNotFoundException e) {
           log.error("Failed to notify friends: {}", e.getMessage() );
        }catch (DataAccessException e){
            log.error("Database error during friend notification", e);
        }
    }
}