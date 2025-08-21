package org.example.chatapp.service;

/**
 * Service interface for managing cache operations related to users.
 * <p>
 * Defines operations for evicting or updating cached data.
 * </p>
 * <p>
 *     For more information look at {@link org.example.chatapp.service.impl.CacheService}
 * </p>
 */
public interface ICacheService {

    void evictFriendsCache(String userEmail);

}
