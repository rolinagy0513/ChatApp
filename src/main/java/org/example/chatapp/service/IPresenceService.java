package org.example.chatapp.service;

import java.util.Map;
import java.util.Set;

/**
 * Service interface for managing user presence status in the chat application.
 * <p>
 * Defines operations for setting users online/offline and retrieving their current presence.
 * </p>
 * <p>
 *      For more information look at {@link org.example.chatapp.service.impl.PresenceService}
 * </p>
 */
public interface IPresenceService {

    void setOnline(String userEmail);

    void setOffline(String userEmail);

    boolean isOnline(String userEmail);

    Set<String> getOnlineUsers();

    Map<String, Boolean> getBulkStatus(Set<String> emails);
}
