package org.example.chatapp.service.impl;

import org.example.chatapp.service.IPresenceService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for tracking online user presence in-memory.
 * - This service exists to ensure fast, real-time access to user presence information.
 * -
 * - Storing "isOnline" as a field in the database would lead to unnecessary I/O operations,
 * - causing micro-lags and performance degradation — especially in high-frequency scenarios
 * - like frequent logins/logouts or heartbeat pings.
 * -
 * - By using an in-memory data structure ({@link ConcurrentHashMap}), this service provides
 * - low-latency access and updates to the online users list, making it ideal for real-time
 * - features such as showing which users are currently online.
 * -
 * Note: This approach is suitable for single-instance deployments.
 * - In the future this will be handled by Redis to make sure the tracking is good for multiple instances
 */

@Service
public class PresenceService implements IPresenceService {

    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void setOnline(String userEmail) {

        if (userEmail == null) return;
        onlineUsers.add(userEmail);
    }

    public void setOffline(String userEmail) {

        if (userEmail == null) return;
        onlineUsers.remove(userEmail);
    }

    public boolean isOnline(String userEmail) {

        if (userEmail == null){
            throw new IllegalStateException("Email can not be null");
        }
        return onlineUsers.contains(userEmail);
    }

    public Set<String> getOnlineUsers() {
        return Collections.unmodifiableSet(onlineUsers);
    }

    /**
     * This method gets all the friends emails and checks their status in one query
     * It was made to ensure good performance in the getAllFriendships method in FriendService
     * @param emails The list that contains all the user's friend's emails
     * @return Returns a boolean value that will set the status of the friend to either ONLINE or OFFLINE
     */
    public Map<String, Boolean> getBulkStatus(Set<String> emails) {
        Map<String, Boolean> result = new HashMap<>();
        emails.forEach(email ->
                result.put(email, onlineUsers.contains(email))
        );
        return result;
    }

}
