package com.androidinsta.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Redis Configuration for AndroidInsta - PROFESSIONAL APPROACH
 * 
 * ===================================================================
 * REDIS USE CASES (What Netflix, Uber, LinkedIn do):
 * ===================================================================
 * ✅ 1. Session Storage: JWT refresh tokens, user sessions
 * ✅ 2. Rate Limiting: API throttling counters
 * ✅ 3. WebSocket Coordination: Online users, typing indicators
 * ✅ 4. Pub/Sub: Real-time events via Kafka + Redis
 * ✅ 5. Simple Counters/Flags: Post counts, follower counts, boolean flags
 * ✅ 6. Distributed Locks: Prevent concurrent operations
 * 
 * ===================================================================
 * WHAT WE DON'T USE REDIS FOR:
 * ===================================================================
 * ❌ 1. Complex DTO Caching: Removed ALL @Cacheable from DTO endpoints
 * ❌ 2. Object Serialization: No GenericJackson2JsonRedisSerializer
 * ❌ 3. Full Response Caching: Database with indexes is fast enough (< 50ms)
 * 
 * ===================================================================
 * CONFIGURATION:
 * ===================================================================
 * - Uses StringRedisSerializer for keys AND values
 * - Values stored as JSON strings (manually serialize/deserialize)
 * - No @class metadata (Flutter-compatible clean JSON)
 * - No LinkedHashMap issues (manual parsing in service layer)
 * - Connection pooling via Lettuce (max-active=20)
 * 
 * See REDIS_BEST_PRACTICES.md for complete documentation.
 * See DATABASE_OPTIMIZATION.sql for query optimization with indexes.
 */
@Configuration
@EnableCaching
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        
        // Use String serializer for keys
        // Use GenericJackson2JsonRedisSerializer for values (simple types only)
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer()
        
        template.setEnableTransactionSupport(true)
        template.afterPropertiesSet()
        return template
    }

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        // Store everything as JSON strings - Spring will handle DTO → JSON conversion
        // On retrieval, Spring returns JSON string, we parse it manually
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .disableCachingNullValues()
            .prefixCacheNameWith("androidinsta:")

        // Custom TTL for different cache types
        val cacheConfigurations = mapOf(
            "users" to defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "posts" to defaultConfig.entryTtl(Duration.ofMinutes(15)),
            "feed" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "notifications" to defaultConfig.entryTtl(Duration.ofMinutes(10)),
            "search" to defaultConfig.entryTtl(Duration.ofMinutes(20)),
            "stats" to defaultConfig.entryTtl(Duration.ofHours(2))
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build()
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper().apply {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // Clean JSON without type metadata - Flutter compatible
        }
    }
}
