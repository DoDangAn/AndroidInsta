package com.androidinsta.controller

import com.androidinsta.Service.RedisService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Monitoring Controller
 * 
 * Provides endpoints for monitoring Redis and Kafka health
 */
@RestController
@RequestMapping("/api/monitoring")
class MonitoringController(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val redisService: RedisService
) {

    /**
     * Check Redis health and connection
     */
    @GetMapping("/redis/health")
    fun checkRedisHealth(): ResponseEntity<Map<String, Any>> {
        return try {
            // Test Redis connection
            redisTemplate.opsForValue().set("health:check", "ok")
            val result = redisTemplate.opsForValue().get("health:check")
            redisTemplate.delete("health:check")
            
            if (result == "ok") {
                ResponseEntity.ok(mapOf(
                    "status" to "UP",
                    "service" to "Redis",
                    "message" to "Redis is healthy and responding"
                ))
            } else {
                ResponseEntity.status(503).body(mapOf(
                    "status" to "DOWN",
                    "service" to "Redis",
                    "message" to "Redis response mismatch"
                ))
            }
        } catch (e: Exception) {
            ResponseEntity.status(503).body(mapOf(
                "status" to "DOWN",
                "service" to "Redis",
                "message" to "Redis connection failed: ${e.message}"
            ))
        }
    }

    /**
     * Get Redis statistics
     */
    @GetMapping("/redis/stats")
    fun getRedisStats(): ResponseEntity<Map<String, Any>> {
        return try {
            val stats = mutableMapOf<String, Any>(
                "status" to "UP",
                "service" to "Redis"
            )
            
            // Try to get some basic stats
            val keys = redisTemplate.keys("*")
            stats["totalKeys"] = keys?.size ?: 0
            
            // Count by prefix
            val tokenBlacklist = keys?.filter { it.startsWith("token:blacklist:") }?.size ?: 0
            val userCache = keys?.filter { it.startsWith("user:cache:") }?.size ?: 0
            val sessions = keys?.filter { it.startsWith("session:") }?.size ?: 0
            val rateLimit = keys?.filter { it.startsWith("rate:limit:") }?.size ?: 0
            
            stats["keysByType"] = mapOf<String, Any>(
                "tokenBlacklist" to tokenBlacklist,
                "userCache" to userCache,
                "sessions" to sessions,
                "rateLimit" to rateLimit
            )
            
            ResponseEntity.ok(stats)
        } catch (e: Exception) {
            ResponseEntity.status(503).body(mapOf<String, Any>(
                "status" to "ERROR",
                "message" to (e.message ?: "Unknown error")
            ))
        }
    }

    /**
     * Check Kafka health
     */
    @GetMapping("/kafka/health")
    fun checkKafkaHealth(): ResponseEntity<Map<String, Any>> {
        return try {
            // Try to send a test message
            val testTopic = "health.check"
            kafkaTemplate.send(testTopic, "health-check", mapOf("test" to "ok"))
            
            ResponseEntity.ok(mapOf(
                "status" to "UP",
                "service" to "Kafka",
                "message" to "Kafka is healthy and accepting messages"
            ))
        } catch (e: Exception) {
            ResponseEntity.status(503).body(mapOf(
                "status" to "DOWN",
                "service" to "Kafka",
                "message" to "Kafka connection failed: ${e.message}"
            ))
        }
    }

    /**
     * Get overall system health
     */
    @GetMapping("/health")
    fun getSystemHealth(): ResponseEntity<Map<String, Any>> {
        val redisHealth = try {
            redisTemplate.opsForValue().set("health:check", "ok")
            redisTemplate.delete("health:check")
            "UP"
        } catch (e: Exception) {
            "DOWN"
        }
        
        val kafkaHealth = try {
            kafkaTemplate.send("health.check", "health", mapOf("test" to "ok"))
            "UP"
        } catch (e: Exception) {
            "DOWN"
        }
        
        val overallStatus = if (redisHealth == "UP" && kafkaHealth == "UP") "UP" else "DEGRADED"
        
        return ResponseEntity.ok(mapOf(
            "status" to overallStatus,
            "components" to mapOf(
                "redis" to redisHealth,
                "kafka" to kafkaHealth
            ),
            "timestamp" to System.currentTimeMillis()
        ))
    }
}
