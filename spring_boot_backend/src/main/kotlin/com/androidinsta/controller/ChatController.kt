package com.androidinsta.controller

import com.androidinsta.Model.MessageType
import com.androidinsta.Service.MessageService
import com.androidinsta.Repository.UserRepository
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val messageService: MessageService,
    private val userRepository: UserRepository
) {
    
    /**
     * GET /api/chat/conversations - Lấy danh sách conversations
     */
    @GetMapping("/conversations")
    fun getConversations(): ResponseEntity<ConversationsResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        val conversations = messageService.getConversations(userId).mapNotNull { (partnerId, lastMessage) ->
            val partner = userRepository.findById(partnerId).orElse(null) ?: return@mapNotNull null
            
            ConversationDto(
                user = UserSummaryDto(
                    id = partner.id,
                    username = partner.username,
                    fullName = partner.fullName,
                    avatarUrl = partner.avatarUrl
                ),
                lastMessage = lastMessage?.toDto(),
                unreadCount = messageService.countUnreadMessages(userId, partnerId).toInt()
            )
        }
        
        return ResponseEntity.ok(ConversationsResponse(conversations))
    }
    
    /**
     * GET /api/chat/{userId} - Lấy chat history với một user
     */
    @GetMapping("/{userId}")
    fun getChatHistory(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<ChatHistoryResponse> {
        val currentUserId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val messages = messageService.getChatHistory(currentUserId, userId, pageable)
        
        // Đánh dấu messages là đã đọc
        messageService.markAsRead(currentUserId, userId)
        
        return ResponseEntity.ok(
            ChatHistoryResponse(
                messages = messages.content.map { it.toDto() }.reversed(), // Reverse để mới nhất ở cuối
                currentPage = messages.number,
                totalPages = messages.totalPages,
                totalItems = messages.totalElements
            )
        )
    }
    
    /**
     * POST /api/chat/send - Gửi message
     */
    @PostMapping("/send")
    fun sendMessage(@RequestBody request: SendMessageRequest): ResponseEntity<MessageDto> {
        val senderId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        val messageType = try {
            MessageType.valueOf(request.messageType.uppercase())
        } catch (e: Exception) {
            MessageType.TEXT
        }
        
        val message = messageService.sendMessage(
            senderId = senderId,
            receiverId = request.receiverId,
            content = request.content,
            mediaUrl = request.mediaUrl,
            messageType = messageType
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(message.toDto())
    }
    
    /**
     * DELETE /api/chat/{messageId} - Xóa message
     */
    @DeleteMapping("/{messageId}")
    fun deleteMessage(@PathVariable messageId: Long): ResponseEntity<Unit> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        messageService.deleteMessage(messageId, userId)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * PUT /api/chat/read/{userId} - Đánh dấu tất cả messages từ userId là đã đọc
     */
    @PutMapping("/read/{userId}")
    fun markAsRead(@PathVariable userId: Long): ResponseEntity<Unit> {
        val currentUserId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        messageService.markAsRead(currentUserId, userId)
        return ResponseEntity.ok().build()
    }
}
