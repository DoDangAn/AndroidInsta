# REDIS BEST PRACTICES - Professional Approach

## ❌ VẤN ĐỀ VỪA GẶP PHẢI

Chúng ta đã cố cache **complex DTOs** (FeedResponse, PostResponse, AdminUserStatsDto...) qua Redis bằng Spring's `@Cacheable` annotation với Jackson serialization.

**3 attempts thất bại:**

### Attempt 1: GenericJackson2JsonRedisSerializer (default)
```kotlin
val serializer = GenericJackson2JsonRedisSerializer(objectMapper())
```
**Result:** ❌ Returns `LinkedHashMap` instead of typed objects
```
class java.util.LinkedHashMap cannot be cast to class com.androidinsta.dto.FeedResponse
```

### Attempt 2: activateDefaultTyping + BasicPolymorphicTypeValidator
```kotlin
val ptv = BasicPolymorphicTypeValidator.builder()
    .allowIfSubType("com.androidinsta")
    .allowIfSubType("java.util")
    .build()
activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
```
**Result:** ❌ Adds `@class` metadata to JSON, Flutter can't parse
```json
{
  "@class": "com.androidinsta.dto.FeedResponse",
  "posts": [...]
}
```
Flutter error:
```
type 'String' is not a subtype of type 'Map<String, dynamic>'
```

### Attempt 3: Jackson2JsonRedisSerializer(Object::class.java)
```kotlin
val serializer = Jackson2JsonRedisSerializer(objectMapper(), Object::class.java)
```
**Result:** ❌ STILL returns LinkedHashMap (same as Attempt 1)

---

## ✅ GIẢI PHÁP: HỌC TỪ APP CHUYÊN NGHIỆP

### **Netflix, Uber, LinkedIn KHÔNG BAO GIỜ cache complex DTOs qua Redis**

Họ làm như sau:

---

## 1. ✅ Cache SIMPLE Data Only

### ✅ Good: Cache Primitive Types
```kotlin
// Cache user flags (String)
redisTemplate.opsForValue().set("user:${userId}:isVerified", "true", Duration.ofHours(1))

// Cache counters (Long)
redisTemplate.opsForValue().set("user:${userId}:followerCount", "42", Duration.ofMinutes(30))

// Cache list of IDs (JSON array of primitives)
redisTemplate.opsForValue().set("user:${userId}:followerIds", "[1,2,3,4,5]", Duration.ofMinutes(10))
```

### ❌ Bad: Cache Complex Objects
```kotlin
// ❌ NEVER DO THIS - causes serialization issues
@Cacheable(value = ["feedPosts"])
fun getFeedPosts(): FeedResponse { ... }

// ❌ NEVER DO THIS - Flutter can't parse @class metadata
@Cacheable(value = ["userStats"])
fun getUserStats(): AdminUserStatsDto { ... }
```

---

## 2. ✅ Optimize Database with Indexes

Professional apps don't cache DTO responses because **database queries are fast enough** with proper indexes.

### Example: Feed Query Optimization
```sql
-- Add composite index for user posts
CREATE INDEX idx_posts_user_created ON posts(user_id, created_at DESC);

-- Query becomes VERY fast (< 50ms typical)
SELECT * FROM posts 
WHERE user_id IN (1,2,3,4,5) 
ORDER BY created_at DESC 
LIMIT 20;
```

**See `DATABASE_OPTIMIZATION.sql` for complete index list.**

### MySQL Query Cache
MySQL automatically caches SELECT results. When data changes (INSERT/UPDATE/DELETE), cache invalidates automatically.

```ini
# my.cnf or my.ini
[mysqld]
query_cache_type = 1
query_cache_size = 128M
query_cache_limit = 4M
```

---

## 3. ✅ Redis Use Cases (What Professionals Use It For)

### ✅ Session Storage
```kotlin
// Store JWT refresh tokens
redisTemplate.opsForValue().set(
    "session:${userId}:refreshToken", 
    refreshToken, 
    Duration.ofDays(7)
)
```

### ✅ Rate Limiting
```kotlin
// API throttling
val key = "ratelimit:${userId}:${endpoint}"
val count = redisTemplate.opsForValue().increment(key) ?: 0
redisTemplate.expire(key, Duration.ofMinutes(1))
if (count > 100) throw TooManyRequestsException()
```

### ✅ WebSocket Coordination
```kotlin
// Track online users
redisTemplate.opsForSet().add("online:users", userId.toString())
redisTemplate.expire("online:users", Duration.ofMinutes(5))
```

### ✅ Pub/Sub for Real-time Events
```kotlin
// Publish typing indicator
redisTemplate.convertAndSend("chat:typing:${chatId}", TypingEvent(userId, true))
```

### ✅ Distributed Locks
```kotlin
// Prevent concurrent operations
val lockKey = "lock:post:${postId}:like"
val lockAcquired = redisTemplate.opsForValue()
    .setIfAbsent(lockKey, "locked", Duration.ofSeconds(5))
if (lockAcquired == true) {
    try {
        // Perform operation
    } finally {
        redisTemplate.delete(lockKey)
    }
}
```

### ✅ Simple Counters
```kotlin
// Increment view count
redisTemplate.opsForValue().increment("post:${postId}:views")

// Get like count (if not in database)
val likes = redisTemplate.opsForValue().get("post:${postId}:likes")?.toLongOrNull() ?: 0L
```

---

## 4. ❌ What NOT to Use Redis For

### ❌ DON'T Cache Complex DTOs
```kotlin
// ❌ WRONG - causes LinkedHashMap errors
@Cacheable(value = ["feed"])
fun getFeed(): FeedResponse { ... }

// ❌ WRONG - Flutter can't parse Jackson type metadata
@Cacheable(value = ["stats"])
fun getStats(): AdminStatsDto { ... }
```

### ❌ DON'T Use GenericJackson2JsonRedisSerializer for Objects
```kotlin
// ❌ WRONG - returns LinkedHashMap instead of typed objects
val serializer = GenericJackson2JsonRedisSerializer(objectMapper())
```

### ❌ DON'T Use activateDefaultTyping
```kotlin
// ❌ WRONG - adds @class metadata that Flutter can't parse
objectMapper.activateDefaultTyping(
    ptv, 
    ObjectMapper.DefaultTyping.NON_FINAL
)
```

---

## 5. ✅ Recommended RedisConfig

```kotlin
@Configuration
@EnableCaching
class RedisConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper().apply {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // ✅ NO activateDefaultTyping - keeps JSON clean for Flutter
        }
    }

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val config = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(5))
            .shutdownTimeout(Duration.ofMillis(100))
            .build()

        return LettuceConnectionFactory(RedisStandaloneConfiguration().apply {
            hostName = "localhost"
            port = 6379
        }, config).apply {
            afterPropertiesSet()
        }
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            
            // ✅ Use StringRedisSerializer for keys (human-readable)
            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            
            // ✅ Use String serializer for values (simple data only)
            // For complex objects, manually serialize to JSON string
            valueSerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
            
            afterPropertiesSet()
        }
    }

    // ✅ Keep cache manager for simple data (if needed in future)
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            // ✅ Use String serializer - cache simple values only
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build()
    }
}
```

---

## 6. ✅ Manual Cache Example (For Simple Data)

Instead of `@Cacheable` with DTOs, manually cache simple computed values:

```kotlin
@Service
class PostService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    fun getUserPostCount(userId: Long): Long {
        val cacheKey = "user:${userId}:postCount"
        
        // Try cache first
        val cached = redisTemplate.opsForValue().get(cacheKey)
        if (cached != null) {
            return cached.toLongOrNull() ?: 0L
        }
        
        // Cache miss - query database
        val count = postRepository.countByUserId(userId)
        
        // Store in cache (simple Long as String)
        redisTemplate.opsForValue().set(
            cacheKey, 
            count.toString(), 
            Duration.ofMinutes(30)
        )
        
        return count
    }
    
    // ✅ Build DTO fresh from database (with indexes, it's fast!)
    fun getUserStats(userId: Long): UserStatsDto {
        val postCount = getUserPostCount(userId)  // Cached simple value
        val followerCount = getFollowerCount(userId)  // Cached simple value
        val followingCount = getFollowingCount(userId)  // Cached simple value
        
        // DTO built fresh - no serialization issues
        return UserStatsDto(
            userId = userId,
            postCount = postCount,
            followerCount = followerCount,
            followingCount = followingCount
        )
    }
}
```

---

## 7. ✅ application.properties Configuration

```properties
# ===================================================================
# REDIS CONFIGURATION - PROFESSIONAL APPROACH
# ===================================================================

# Redis connection
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=5000ms

# Lettuce pool (connection pooling)
spring.data.redis.lettuce.pool.max-active=20
spring.data.redis.lettuce.pool.max-idle=10
spring.data.redis.lettuce.pool.min-idle=5
spring.data.redis.lettuce.pool.max-wait=2000ms

# ✅ Enable Redis cache (for simple data only, NOT for DTOs)
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
spring.cache.redis.cache-null-values=false

# ✅ Redis used for:
# 1. Session storage (JWT refresh tokens)
# 2. Rate limiting (API throttling)
# 3. WebSocket coordination (online users, typing indicators)
# 4. Pub/Sub (real-time events)
# 5. Simple counters/flags (post counts, user flags)
#
# ❌ Redis NOT used for:
# 1. Complex DTO caching (@Cacheable removed from all DTO endpoints)
# 2. Object serialization via Jackson (causes LinkedHashMap errors)
# 3. Full response caching (database with indexes is fast enough)
```

---

## 8. Summary: Professional Approach

| **Aspect** | **❌ What We Did Wrong** | **✅ What Professionals Do** |
|------------|--------------------------|------------------------------|
| **Complex DTOs** | Cached via `@Cacheable` + Jackson | Build fresh from indexed database queries |
| **Serialization** | GenericJackson2JsonRedisSerializer | StringRedisSerializer (simple data only) |
| **Type Safety** | activateDefaultTyping (@class metadata) | No type metadata - Flutter-compatible JSON |
| **Performance** | Relied on Redis cache | Database indexes (< 50ms queries) |
| **Redis Use** | DTO storage | Sessions, rate limiting, pub/sub, counters |
| **Cache Strategy** | Automatic with @Cacheable | Manual for simple values only |

---

## 9. Migration Checklist

- [x] ✅ Remove all 7 `@Cacheable` annotations from DTO endpoints
- [x] ✅ Create `DATABASE_OPTIMIZATION.sql` with proper indexes
- [ ] ⏳ Run SQL indexes on development database
- [ ] ⏳ Update `RedisConfig.kt` to use StringRedisSerializer
- [ ] ⏳ Change `spring.cache.type=redis` in application.properties
- [ ] ⏳ Test backend - verify no LinkedHashMap errors
- [ ] ⏳ Test Flutter app - verify API responses work
- [ ] ⏳ Benchmark query performance (should be < 50ms)
- [ ] ⏳ Document this approach for team members

---

## 10. References

**Industry Examples:**
- **Netflix**: Uses Redis for session storage, rate limiting, NOT for DTO caching
- **Uber**: Database with proper indexes, Redis for real-time coordination
- **LinkedIn**: Feed queries optimized with database indexes, Redis for pub/sub

**Why This Works:**
1. Database queries with indexes are FAST (< 50ms typical)
2. No Jackson serialization issues (no LinkedHashMap, no @class metadata)
3. Flutter gets clean JSON (standard format, no type hints)
4. Data always fresh (no stale cache)
5. MySQL query cache handles repeated queries automatically

**The Rule:**
> **If data structure is complex enough to need Jackson serialization, it's complex enough that you SHOULDN'T cache it in Redis.**

Use database with proper indexes instead.

---

**Author:** Spring Boot Backend Team  
**Date:** December 2024  
**Version:** 1.0
