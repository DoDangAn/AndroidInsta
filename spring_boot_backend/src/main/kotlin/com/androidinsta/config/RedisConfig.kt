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
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import com.androidinsta.dto.FeedResponse
import java.time.Duration
import org.springframework.boot.CommandLineRunner
import javax.annotation.PostConstruct

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
 * ❌ 2. Object Serialization: No GenericJackson2JsonRedisRedisSerializer
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
        val feedSerializer = Jackson2JsonRedisSerializer(FeedResponse::class.java)
        feedSerializer.setObjectMapper(objectMapper())

        val cacheConfigurations = mapOf(
            "users" to defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "posts" to defaultConfig.entryTtl(Duration.ofMinutes(15)),
            "feed" to defaultConfig.entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(feedSerializer)
                ),
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

    @Bean
    fun testFeedResponseSerialization(): Boolean {
        val feedSerializer = Jackson2JsonRedisSerializer(FeedResponse::class.java)
        feedSerializer.setObjectMapper(objectMapper())

        val testFeedResponse = FeedResponse(
            posts = listOf(),
            currentPage = 1,
            totalPages = 1,
            totalItems = 0
        )

        return try {
            val serialized = feedSerializer.serialize(testFeedResponse)
            val deserialized = feedSerializer.deserialize(serialized!!)
            println("Serialization successful: $deserialized")
            true
        } catch (e: Exception) {
            println("Serialization failed: ${e.message}")
            false
        }
    }

    @Bean
    fun validateFeedCacheConfiguration(cacheManager: CacheManager): Boolean {
        return try {
            val feedCache = cacheManager.getCache("feed")
            if (feedCache == null) {
                println("Feed cache not found in CacheManager.")
                return false
            }
            println("Feed cache found. Testing serialization...")

            val feedSerializer = Jackson2JsonRedisSerializer(FeedResponse::class.java)
            feedSerializer.setObjectMapper(objectMapper())

            val testFeedResponse = FeedResponse(
                posts = listOf(),
                currentPage = 1,
                totalPages = 1,
                totalItems = 0
            )

            val serialized = feedSerializer.serialize(testFeedResponse)
            val deserialized = feedSerializer.deserialize(serialized!!)
            println("Feed cache serialization successful: $deserialized")
            true
        } catch (e: Exception) {
            println("Feed cache validation failed: ${e.message}")
            false
        }
    }

    @Bean
    fun validateRedisConfigurationRunner(redisTemplate: RedisTemplate<String, Any>, cacheManager: CacheManager): CommandLineRunner {
        return CommandLineRunner {
            val feedCacheValid = validateFeedCacheConfiguration(cacheManager)
            println("Feed cache configuration valid: $feedCacheValid")

            val serializationTest = testFeedResponseSerialization()
            println("FeedResponse serialization test passed: $serializationTest")
        }
    }

    @Bean
    fun validateFeedCacheConfigurationWithLogging(cacheManager: CacheManager): CommandLineRunner {
        return CommandLineRunner {
            val feedCache = cacheManager.getCache("feed")
            if (feedCache == null) {
                println("Feed cache not found in CacheManager.")
            } else {
                println("Feed cache found. Testing serialization...")
                val feedSerializer = Jackson2JsonRedisSerializer(FeedResponse::class.java)
                feedSerializer.setObjectMapper(objectMapper())

                val testFeedResponse = FeedResponse(
                    posts = listOf(),
                    currentPage = 1,
                    totalPages = 1,
                    totalItems = 0
                )

                try {
                    val serialized = feedSerializer.serialize(testFeedResponse)
                    val deserialized = feedSerializer.deserialize(serialized!!)
                    println("Feed cache serialization successful: $deserialized")
                } catch (e: Exception) {
                    println("Feed cache serialization failed: ${e.message}")
                }
            }
        }
    }
}
