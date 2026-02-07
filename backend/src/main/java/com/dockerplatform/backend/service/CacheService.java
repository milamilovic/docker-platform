package com.dockerplatform.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@ConditionalOnBean(CacheManager.class)
public class CacheService {

    @Autowired
    private CacheManager cacheManager;

    public void clearRepositoryCaches() {
        clearCache("repositories");
        clearCache("repository");
        clearCache("myRepositories");
        clearCache("officialRepositories");
        clearCache("myOfficialRepositories");
    }

    public void clearTagCaches() {
        clearCache("tags");
        clearCache("repositoryTags");
    }

    public void clearAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }

    public void clearCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    public void evictFromCache(String cacheName, Object key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}