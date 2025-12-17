package com.androidinsta.Service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Redis Service for AndroidInsta - PROFESSIONAL APPROACH
 * 
 * Provides high-level Redis operations for:
 * - Token blacklisting (logout, security)
 * - Simple counter caching (like counts, comment counts, follower counts)
 * - Rate limiting
 * - Session management
 * - Generic key-value operations
 * 
 * ❌ NOT used for:
 * - Complex DTO caching (removed all @Cacheable from DTOs)
 * - Object serialization (no LinkedHashMap issues)
 */
@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    private val logger = LoggerFactory.getLogger(RedisService::class.java)

    companion object {
        private const val TOKEN_BLACKLIST_PREFIX = "token:blacklist:"
        private const val USER_CACHE_PREFIX = "user:cache:"
        private const val RATE_LIMIT_PREFIX = "rate:limit:"
        private const val SESSION_PREFIX = "session:"
        private const val ONLINE_USERS_PREFIX = "online:users:"
        private const val TRENDING_PREFIX = "trending:"
    }

    // ===========================
    // Token Blacklist Operations
    // ===========================
    
    /**
     * Blacklist a token (for logout or token revocation)
     */
    fun blacklistToken(token: String, expirationMillis: Long) {
        try {
            val key = TOKEN_BLACKLIST_PREFIX + token
            redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofMillis(expirationMillis))
            logger.debug("Token blacklisted: ${token.take(20)}...")
        } catch (e: Exception) {
            logger.error("Error blacklisting token", e)
        }
    }

    /**
     * Check if token is blacklisted
     */
    fun isTokenBlacklisted(token: String): Boolean {
        return try {
            val key = TOKEN_BLACKLIST_PREFIX + token
            redisTemplate.hasKey(key)
        } catch (e: Exception) {
            logger.error("Error checking token blacklist", e)
            false
        }
    }

    // ===========================
    // User Cache Operations
    // ===========================
    
    /**
     * Cache user data (prefer simple String/Number, avoid complex objects)
     */
    fun cacheUser(userId: Long, userData: Any, ttlMinutes: Long = 30) {
        try {
            val key = USER_CACHE_PREFIX + userId
            redisTemplate.opsForValue().set(key, userData, Duration.ofMinutes(ttlMinutes))
            logger.debug("Cached user: $userId")
        } catch (e: Exception) {
            logger.error("Error caching user $userId", e)
        }
    }

    /**
     * Get cached user data
     */
    fun getCachedUser(userId: Long): Any? {
        return try {
            val key = USER_CACHE_PREFIX + userId
            redisTemplate.opsForValue().get(key)
        } catch (e: Exception) {
            logger.error("Error getting cached user $userId", e)
            null
        }
    }

    /**
     * Invalidate user cache
     */
    fun invalidateUserCache(userId: Long) {
        try {
            val key = USER_CACHE_PREFIX + userId
            redisTemplate.delete(key)
            logger.debug("Invalidated user cache: $userId")
        } catch (e: Exception) {
            logger.error("Error invalidating user cache $userId", e)
        }
    }

    // ===========================
    // Rate Limiting Operations
    // ===========================
    
    /**
     * Rate limiting check (e.g., API rate limiting)
     * @return true if request is allowed, false if rate limit exceeded
     */
    fun checkRateLimit(identifier: String, maxRequests: Int, windowSeconds: Long): Boolean {
        return try {
            val key = RATE_LIMIT_PREFIX + identifier
            val current = redisTemplate.opsForValue().increment(key) ?: 1L

            if (current == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds))
            }

            val allowed = current <= maxRequests
            if (!allowed) {
                logger.warn("Rate limit exceeded for: $identifier ($current/$maxRequests)")
            }
            allowed
        } catch (e: Exception) {
            logger.error("Error checking rate limit", e)
            true // Allow request on error
        }
    }

    /**
     * Get current rate limit count
     */
    fun getRateLimitCount(identifier: String): Long {
        return try {
            val key = RATE_LIMIT_PREFIX + identifier
            (redisTemplate.opsForValue().get(key) as? Number)?.toLong() ?: 0L
        } catch (e: Exception) {
            logger.error("Error getting rate limit count", e)
            0L
        }
    }

    // ===========================
    // Session Management
    // ===========================
    
    /**
     * Store user session
     */
    fun storeSession(sessionId: String, userId: Long, ttlMinutes: Long = 60) {
        try {
            val key = SESSION_PREFIX + sessionId
            redisTemplate.opsForValue().set(key, userId, Duration.ofMinutes(ttlMinutes))
        } catch (e: Exception) {
            logger.error("Error storing session", e)
        }
    }

    /**
     * Get user ID from session
     */
    fun getSessionUserId(sessionId: String): Long? {
        return try {
            val key = SESSION_PREFIX + sessionId
            (redisTemplate.opsForValue().get(key) as? Number)?.toLong()
        } catch (e: Exception) {
            logger.error("Error getting session", e)
            null
        }
    }

    /**
     * Delete session
     */
    fun deleteSession(sessionId: String) {
        try {
            val key = SESSION_PREFIX + sessionId
            redisTemplate.delete(key)
        } catch (e: Exception) {
            logger.error("Error deleting session", e)
        }
    }

    // ===========================
    // Online Users Tracking
    // ===========================
    
    /**
     * Mark user as online
     */
    fun setUserOnline(userId: Long, ttlMinutes: Long = 5) {
        try {
            val key = ONLINE_USERS_PREFIX + userId
            redisTemplate.opsForValue().set(key, System.currentTimeMillis(), Duration.ofMinutes(ttlMinutes))
        } catch (e: Exception) {
            logger.error("Error setting user online", e)
        }
    }

    /**
     * Check if user is online
     */
    fun isUserOnline(userId: Long): Boolean {
        return try {
            val key = ONLINE_USERS_PREFIX + userId
            redisTemplate.hasKey(key)
        } catch (e: Exception) {
            logger.error("Error checking user online status", e)
            false
        }
    }

    // ===========================
    // Trending/Popular Content
    // ===========================
    
    /**
     * Increment trending score (e.g., for posts, hashtags)
     */
    fun incrementTrendingScore(category: String, itemId: String, score: Double = 1.0) {
        try {
            val key = TRENDING_PREFIX + category
            redisTemplate.opsForZSet().incrementScore(key, itemId, score)
            redisTemplate.expire(key, Duration.ofHours(24))
        } catch (e: Exception) {
            logger.error("Error incrementing trending score", e)
        }
    }

    /**
     * Get top trending items
     */
    fun getTopTrending(category: String, limit: Int = 10): Set<Any> {
        return try {
            val key = TRENDING_PREFIX + category
            redisTemplate.opsForZSet().reverseRange(key, 0, (limit - 1).toLong()) ?: emptySet()
        } catch (e: Exception) {
            logger.error("Error getting top trending", e)
            emptySet()
        }
    }

    // ===========================
    // Generic Operations
    // ===========================
    
    /**
     * Generic set operation (prefer simple types: String, Long, Int, Boolean)
     */
    fun set(key: String, value: Any, ttl: Duration) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl)
        } catch (e: Exception) {
            logger.error("Error setting key: $key", e)
        }
    }

    /**
     * Generic get operation
     */
    fun get(key: String): Any? {
        return try {
            redisTemplate.opsForValue().get(key)
        } catch (e: Exception) {
            logger.error("Error getting key: $key", e)
            null
        }
    }

    /**
     * Generic get operation with type casting
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, clazz: Class<T>): T? {
        return try {
            redisTemplate.opsForValue().get(key) as? T
        } catch (e: Exception) {
            logger.error("Error getting key: $key", e)
            null
        }
    }

    /**
     * Delete key
     */
    fun delete(key: String) {
        try {
            redisTemplate.delete(key)
        } catch (e: Exception) {
            logger.error("Error deleting key: $key", e)
        }
    }

    /**
     * Delete multiple keys by pattern
     */
    fun deletePattern(pattern: String) {
        try {
            val keys = redisTemplate.keys(pattern)
            if (!keys.isNullOrEmpty()) {
                redisTemplate.delete(keys)
                logger.debug("Deleted ${keys.size} keys matching pattern: $pattern")
            }
        } catch (e: Exception) {
            logger.error("Error deleting keys by pattern: $pattern", e)
        }
    }

    /**
     * Check if key exists
     */
    fun exists(key: String): Boolean {
        return try {
            redisTemplate.hasKey(key)
        } catch (e: Exception) {
            logger.error("Error checking key existence: $key", e)
            false
        }
    }

    /**
     * Set expiration time for existing key
     */
    fun expire(key: String, ttl: Duration): Boolean {
        return try {
            redisTemplate.expire(key, ttl) ?: false
        } catch (e: Exception) {
            logger.error("Error setting expiration for key: $key", e)
            false
        }
    }

    /**
     * Get time to live for key
     */
    fun getTTL(key: String): Long {
        return try {
            redisTemplate.getExpire(key, TimeUnit.SECONDS) ?: -1
        } catch (e: Exception) {
            logger.error("Error getting TTL for key: $key", e)
            -1
        }
    }

    /**
     * Increment a numeric key atomically. Returns the new value.
     * If ttl is provided and this is a new key, set the ttl.
     */
    fun increment(key: String, delta: Long = 1, ttl: Duration? = null): Long {
        return try {
            val value = redisTemplate.opsForValue().increment(key, delta) ?: 0L
            if (ttl != null) {
                redisTemplate.expire(key, ttl)
            }
            value
        } catch (e: Exception) {
            logger.error("Error incrementing key: $key", e)
            0L
        }
    }

    /**
     * Push a string entry to a Redis list (left push), trim to `maxLen`, and set optional TTL.
     */
    fun pushToList(key: String, value: String, maxLen: Int = 200, ttl: Duration? = null) {
        try {
            redisTemplate.opsForList().leftPush(key, value)
            if (maxLen > 0) {
                redisTemplate.opsForList().trim(key, 0, (maxLen - 1).toLong())
            }
            if (ttl != null) {
                redisTemplate.expire(key, ttl)
            }
        } catch (e: Exception) {
            logger.error("Error pushing to list $key", e)
        }
    }
}
