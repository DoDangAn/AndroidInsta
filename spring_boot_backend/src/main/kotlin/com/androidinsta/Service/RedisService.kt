package com.androidinsta.Service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    companion object {
        private const val TOKEN_BLACKLIST_PREFIX = "token:blacklist:"
        private const val USER_CACHE_PREFIX = "user:cache:"
        private const val RATE_LIMIT_PREFIX = "rate:limit:"
    }

    /**
     * Blacklist a token (for logout or token revocation)
     */
    fun blacklistToken(token: String, expirationMillis: Long) {
        val key = TOKEN_BLACKLIST_PREFIX + token
        redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofMillis(expirationMillis))
    }

    /**
     * Check if token is blacklisted
     */
    fun isTokenBlacklisted(token: String): Boolean {
        val key = TOKEN_BLACKLIST_PREFIX + token
        return redisTemplate.hasKey(key)
    }

    /**
     * Cache user data
     */
    fun cacheUser(userId: Long, userData: Any, ttlMinutes: Long = 60) {
        val key = USER_CACHE_PREFIX + userId
        redisTemplate.opsForValue().set(key, userData, Duration.ofMinutes(ttlMinutes))
    }

    /**
     * Get cached user data
     */
    fun getCachedUser(userId: Long): Any? {
        val key = USER_CACHE_PREFIX + userId
        return redisTemplate.opsForValue().get(key)
    }

    /**
     * Invalidate user cache
     */
    fun invalidateUserCache(userId: Long) {
        val key = USER_CACHE_PREFIX + userId
        redisTemplate.delete(key)
    }

    /**
     * Rate limiting check (e.g., API rate limiting)
     */
    fun checkRateLimit(identifier: String, maxRequests: Int, windowSeconds: Long): Boolean {
        val key = RATE_LIMIT_PREFIX + identifier
        val current = redisTemplate.opsForValue().increment(key) ?: 1L

        if (current == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds))
        }

        return current <= maxRequests
    }

    /**
     * Generic set operation with TTL
     */
    fun set(key: String, value: Any, ttl: Duration) {
        redisTemplate.opsForValue().set(key, value, ttl)
    }

    /**
     * Generic get operation
     */
    fun get(key: String): Any? {
        return redisTemplate.opsForValue().get(key)
    }

    /**
     * Delete key
     */
    fun delete(key: String) {
        redisTemplate.delete(key)
    }

    /**
     * Check if key exists
     */
    fun exists(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }
}
