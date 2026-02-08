package com.dockerplatform.backend.configs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Use RedisSerializer.json() - works perfectly with CacheablePage
        RedisSerializer<Object> serializer = RedisSerializer.json();

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                )
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues();

        // Custom cache configurations for different entities
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Repository caches - 1 hour TTL
        cacheConfigurations.put("repositories", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("repository", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("myRepositories", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("officialRepositories", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("myOfficialRepositories", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Tag caches - 30 minutes TTL
        cacheConfigurations.put("tags", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("repositoryTags", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}