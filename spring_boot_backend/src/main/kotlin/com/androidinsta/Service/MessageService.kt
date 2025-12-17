package com.androidinsta.Service

import com.androidinsta.Model.Message
import com.androidinsta.Model.MessageType
import com.androidinsta.Repository.User.MessageRepository
import com.androidinsta.Repository.User.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

@Service
@Transactional(readOnly = true)
class MessageService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val redisService: RedisService,
    private val kafkaProducerService: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(MessageService::class.java)
    private val objectMapper = jacksonObjectMapper().registerKotlinModule()
    
    /**
     * Gửi message
     */
    @Transactional
    fun sendMessage(
        senderId: Long,
        receiverId: Long,
        content: String?,
        mediaUrl: String?,
        messageType: MessageType
    ): Message {
        val sender = userRepository.findById(senderId)
            .orElseThrow { RuntimeException("Sender not found") }
        val receiver = userRepository.findById(receiverId)
            .orElseThrow { RuntimeException("Receiver not found") }
        
        val message = Message(
            sender = sender,
            receiver = receiver,
            content = content,
            mediaUrl = mediaUrl,
            messageType = messageType,
            isRead = false
        )
        
        val savedMessage = messageRepository.save(message)

        // Register after-commit work: update Redis counters/lists and publish Kafka event.
        // This avoids publishing events when the DB transaction rolls back.
        try {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronizationAdapter() {
                override fun afterCommit() {
                    try {
                        // Increment per-sender unread and total unread for receiver
                        redisService.increment("unread:messages:$receiverId:$senderId", 1, java.time.Duration.ofDays(30))
                        redisService.increment("unread:total:$receiverId", 1)

                        // Prepare conversation key (stable ordering)
                        val convKey = if (senderId < receiverId) "$senderId:$receiverId" else "$receiverId:$senderId"

                        // Push a lightweight summary JSON into recent messages list
                        val summary = mapOf(
                            "id" to savedMessage.id,
                            "senderId" to senderId,
                            "receiverId" to receiverId,
                            "content" to (content ?: ""),
                            "mediaUrl" to (mediaUrl ?: ""),
                            "createdAt" to (savedMessage.createdAt?.toString() ?: "")
                        )
                        val json = objectMapper.writeValueAsString(summary)
                        redisService.pushToList("chat:history:$convKey", json, 200, java.time.Duration.ofDays(30))

                        // Publish Kafka event for downstream consumers
                        kafkaProducerService.sendMessageSentEvent(
                            messageId = savedMessage.id,
                            senderId = senderId,
                            receiverId = receiverId,
                            content = content ?: ""
                        )
                    } catch (ex: Exception) {
                        logger.error("Error in afterCommit hooks for message ${savedMessage.id}", ex)
                    }
                }
            })
        } catch (e: Exception) {
            logger.error("Failed to register transaction synchronization for message ${savedMessage.id}", e)
            // Best-effort fallback: attempt direct publish/update (do not block)
            try {
                redisService.increment("unread:messages:$receiverId:$senderId", 1, java.time.Duration.ofDays(30))
                redisService.increment("unread:total:$receiverId", 1)
                val convKey = if (senderId < receiverId) "$senderId:$receiverId" else "$receiverId:$senderId"
                val summary = mapOf(
                    "id" to savedMessage.id,
                    "senderId" to senderId,
                    "receiverId" to receiverId,
                    "content" to (content ?: ""),
                    "mediaUrl" to (mediaUrl ?: ""),
                    "createdAt" to (savedMessage.createdAt?.toString() ?: "")
                )
                val json = objectMapper.writeValueAsString(summary)
                redisService.pushToList("chat:history:$convKey", json, 200, java.time.Duration.ofDays(30))
                kafkaProducerService.sendMessageSentEvent(
                    messageId = savedMessage.id,
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content ?: ""
                )
            } catch (ex: Exception) {
                logger.error("Fallback publish failed for message ${savedMessage.id}", ex)
            }
        }

        // Note: WebSocket push được xử lý bởi WebSocketChatController.handleChatMessage()
        // Không gửi message ở đây để tránh duplicate

        return savedMessage
    }
    
    /**
     * Lấy chat history với pagination
     */
    fun getChatHistory(userId: Long, partnerId: Long, pageable: Pageable): Page<Message> {
        // DON'T cache Page<Message> - complex DTO, query is fast with DB indexes
        return messageRepository.findChatHistory(userId, partnerId, pageable)
    }
    
    /**
     * Lấy danh sách conversations
     */
    fun getConversations(userId: Long): List<Pair<Long, Message?>> {
        // DON'T cache conversation list - complex structure with Message entities
        val partnerIds = messageRepository.findChatPartners(userId)
        
        return partnerIds.map { partnerId ->
            val lastMessage = messageRepository.findLastMessage(userId, partnerId)
            partnerId to lastMessage
        }.sortedByDescending { it.second?.createdAt }
    }
    
    /**
     * Đếm unread messages từ một user
     */
    fun countUnreadMessages(receiverId: Long, senderId: Long): Long {
        val cacheKey = "unread:messages:$receiverId:$senderId"
        
        val cached = redisService.get(cacheKey)
        if (cached != null) {
            return when (cached) {
                is Long -> cached
                is Int -> cached.toLong()
                is Number -> cached.toLong()
                else -> 0L
            }
        }
        
        val count = messageRepository.countUnreadMessages(receiverId, senderId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(1))
        
        return count
    }
    
    /**
     * Đánh dấu messages là đã đọc
     */
    @Transactional
    fun markAsRead(receiverId: Long, senderId: Long) {
        messageRepository.markMessagesAsRead(receiverId, senderId)
        
        // Invalidate unread count cache
        redisService.delete("unread:messages:$receiverId:$senderId")
    }
    
    /**
     * Xóa message
     */
    @Transactional
    fun deleteMessage(messageId: Long, userId: Long) {
        val message = messageRepository.findById(messageId)
            .orElseThrow { RuntimeException("Message not found") }
        
        if (message.sender.id != userId) {
            throw RuntimeException("You can only delete your own messages")
        }
        
        val senderId = message.sender.id
        val receiverId = message.receiver.id
        
        // Invalidate caches
        redisService.delete("conversation:$senderId")
        redisService.delete("conversation:$receiverId")
        redisService.delete("chat:history:$senderId:$receiverId:*")
        redisService.delete("chat:history:$receiverId:$senderId:*")
        
        // Send Kafka event
        kafkaProducerService.sendMessageDeletedEvent(
            messageId = messageId,
            userId = message.sender.id
        )
        
        messageRepository.delete(message)
    }
}
