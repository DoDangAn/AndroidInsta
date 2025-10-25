package com.androidinsta.Service

import com.androidinsta.Model.Message
import com.androidinsta.Model.MessageType
import com.androidinsta.Repository.MessageRepository
import com.androidinsta.Repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MessageService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {
    
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
        
        // Gửi message qua WebSocket đến receiver
        messagingTemplate.convertAndSendToUser(
            receiver.username,
            "/queue/messages",
            savedMessage
        )
        
        return savedMessage
    }
    
    /**
     * Lấy chat history với pagination
     */
    fun getChatHistory(userId: Long, partnerId: Long, pageable: Pageable): Page<Message> {
        return messageRepository.findChatHistory(userId, partnerId, pageable)
    }
    
    /**
     * Lấy danh sách conversations
     */
    fun getConversations(userId: Long): List<Pair<Long, Message?>> {
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
        return messageRepository.countUnreadMessages(receiverId, senderId)
    }
    
    /**
     * Đánh dấu messages là đã đọc
     */
    @Transactional
    fun markAsRead(receiverId: Long, senderId: Long) {
        messageRepository.markMessagesAsRead(receiverId, senderId)
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
        
        messageRepository.delete(message)
    }
}
