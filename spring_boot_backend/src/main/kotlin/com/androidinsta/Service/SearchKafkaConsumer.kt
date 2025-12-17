package com.androidinsta.Service

import com.androidinsta.config.KafkaConfig
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class SearchKafkaConsumer(
    private val redisService: RedisService
) {

    private val logger = LoggerFactory.getLogger(SearchKafkaConsumer::class.java)

    @KafkaListener(topics = [KafkaConfig.POST_CREATED_TOPIC, KafkaConfig.POST_UPDATED_TOPIC, KafkaConfig.POST_DELETED_TOPIC])
    fun handlePostEvents(payload: Map<String, Any>) {
        try {
            // Basic behavior: invalidate search preview caches and update trending score
            val postId = (payload["postId"] as? Number)?.toLong()
            logger.info("SearchKafkaConsumer received post event for postId=${postId}")

            // Invalidate preview caches
            redisService.deletePattern("search:all:preview:*")

            // Optionally update trending score if present
            if (postId != null) {
                redisService.incrementTrendingScore("posts", postId.toString(), 1.0)
            }
        } catch (e: Exception) {
            logger.error("Error handling post event for search cache", e)
        }
    }
}
