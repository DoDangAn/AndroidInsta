package com.androidinsta.controller.User

import com.androidinsta.Model.MessageType
import com.androidinsta.Service.MessageService
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import jakarta.validation.Valid
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller cho chat operations
 * Handles conversations, chat history, and message sending via REST API
 */
@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val messageService: MessageService,
    private val userRepository: UserRepository
) {
    
    /**
     * Lấy danh sách conversations
     * GET /api/chat/conversations
     */
    @GetMapping("/conversations")
    fun getConversations(): ResponseEntity<ConversationsResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val conversations = messageService.getConversations(userId).mapNotNull { (partnerId, lastMessage) ->
            val partner = userRepository.findById(partnerId).orElse(null) ?: return@mapNotNull null
            
            ConversationDto(
                userId = partner.id,
                username = partner.username,
                avatarUrl = partner.avatarUrl,
                fullName = partner.fullName,
                lastMessage = lastMessage?.content,
                lastMessageTime = lastMessage?.createdAt,
                unreadCount = messageService.countUnreadMessages(userId, partnerId).toInt()
            )
        }
        
        return ResponseEntity.ok(
            ConversationsResponse(
                success = true,
                message = "Conversations retrieved successfully",
                data = conversations
            )
        )
    }
    
    /**
     * Lấy chat history với một user
     * GET /api/chat/{userId}
     */
    @GetMapping("/{userId}")
    fun getChatHistory(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<ChatHistoryResponse> {
        val currentUserId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val messages = messageService.getChatHistory(currentUserId, userId, pageable)
        
        messageService.markAsRead(currentUserId, userId)
        
        return ResponseEntity.ok(
            ChatHistoryResponse(
                messages = messages.content.map { it.toDto() }.reversed(),
                currentPage = messages.number,
                totalPages = messages.totalPages,
                totalMessages = messages.totalElements
            )
        )
    }
    
    /**
     * Gửi message
     * POST /api/chat/send
     */
    
    /**
     * Xóa message
     * DELETE /api/chat/{messageId}
     */
    @DeleteMapping("/{messageId}")
    fun deleteMessage(@PathVariable messageId: Long): ResponseEntity<Unit> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        messageService.deleteMessage(messageId, userId)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * Đánh dấu tất cả messages từ userId là đã đọc
     * PUT /api/chat/read/{userId}
     */
    @PutMapping("/read/{userId}")
    fun markAsRead(@PathVariable userId: Long): ResponseEntity<Unit> {
        val currentUserId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        messageService.markAsRead(currentUserId, userId)
        return ResponseEntity.ok().build()
    }
}
