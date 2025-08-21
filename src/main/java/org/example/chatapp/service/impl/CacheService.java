package org.example.chatapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chatapp.service.ICacheService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * A dedicated service for cache eviction operations.
 * -
 * This service exists due to a limitation in Spring's proxy-based AOP model:
 * {@code @CacheEvict} (and other AOP-based annotations like {@code @Transactional})
 * are only triggered when the annotated method is called from outside the bean,
 * i.e., through the Spring proxy.
 * -
 * If an eviction method is called from within the same class (self-invocation),
 * the annotation will not take effect. To ensure proper cache eviction,
 * these methods are moved to a separate bean, allowing them to be called
 * through the Spring proxy and trigger the eviction as expected.
 */


@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService implements ICacheService {

    @CacheEvict(value = "friends", key = "#userEmail")
    public void evictFriendsCache(String userEmail){
    }

}
