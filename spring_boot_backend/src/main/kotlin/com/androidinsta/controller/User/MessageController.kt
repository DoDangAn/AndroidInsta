package com.androidinsta.controller.User

import com.androidinsta.Service.MessageService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import com.androidinsta.Model.MessageType
import com.androidinsta.Repository.User.UserRepository
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller cho message operations
 * Handles sending, receiving, and managing direct messages
 */
@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = ["*"])
class MessageController(
    private val messageService: MessageService,
    private val userRepository: UserRepository
) {

    /**
     * Gửi tin nhắn
     * POST /api/messages
     */
    @PostMapping
    fun sendMessage(@Valid @RequestBody request: SendMessageRequest): ResponseEntity<SendMessageResponse> {
        val senderId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val messageType = when (request.messageType.uppercase()) {
            "IMAGE" -> MessageType.image
            "VIDEO" -> MessageType.video
            else -> MessageType.text
        }

        val message = messageService.sendMessage(
            senderId = senderId,
            receiverId = request.receiverId,
            content = request.content,
            mediaUrl = request.mediaUrl,
            messageType = messageType
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            SendMessageResponse(
                success = true,
                message = "Message sent successfully",
                data = message.toDto()
            )
        )
    }

    /**
     * Lấy danh sách conversations
     * GET /api/messages/conversations
     */
    @GetMapping("/conversations")
    fun getConversations(): ResponseEntity<ConversationsResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val conversations = messageService.getConversations(userId)
        
        val conversationDtos = conversations.mapNotNull { (partnerId, lastMessage) ->
            val partner = userRepository.findById(partnerId).orElse(null) ?: return@mapNotNull null
            val unreadCount = messageService.countUnreadMessages(userId, partnerId)
            
            ConversationDto(
                userId = partner.id,
                username = partner.username,
                avatarUrl = partner.avatarUrl,
                fullName = partner.fullName,
                lastMessage = lastMessage?.content,
                lastMessageTime = lastMessage?.createdAt,
                unreadCount = unreadCount.toInt()
            )
        }

        return ResponseEntity.ok(
            ConversationsResponse(
                success = true,
                message = "Conversations retrieved successfully",
                data = conversationDtos
            )
        )
    }

    /**
     * Lấy lịch sử chat với một user
     * GET /api/messages/chat/{partnerId}
     */
    @GetMapping("/chat/{partnerId}")
    fun getChatHistory(
        @PathVariable partnerId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<MessagesResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val messagesPage = messageService.getChatHistory(userId, partnerId, pageable)
        
        val messageDtos = messagesPage.content.map { it.toDto() }

        return ResponseEntity.ok(
            MessagesResponse(
                success = true,
                message = "Chat history retrieved successfully",
                data = MessagesData(
                    messages = messageDtos,
                    currentPage = messagesPage.number,
                    totalPages = messagesPage.totalPages,
                    totalItems = messagesPage.totalElements
                )
            )
        )
    }

    /**
     * Đếm tin nhắn chưa đọc từ một user
     * GET /api/messages/unread/{partnerId}
     */
    @GetMapping("/unread/{partnerId}")
    fun getUnreadCount(@PathVariable partnerId: Long): ResponseEntity<CountResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val count = messageService.countUnreadMessages(userId, partnerId)
        return ResponseEntity.ok(CountResponse(success = true, count = count))
    }

    /**
     * Đánh dấu tất cả tin nhắn từ một user là đã đọc
     * PUT /api/messages/read/{partnerId}
     */
    @PutMapping("/read/{partnerId}")
    fun markAsRead(@PathVariable partnerId: Long): ResponseEntity<MessageResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        messageService.markAsRead(userId, partnerId)
        return ResponseEntity.ok(MessageResponse(success = true, message = "Messages marked as read"))
    }

    /**
     * Xóa tin nhắn
     * DELETE /api/messages/{messageId}
     */
    @DeleteMapping("/{messageId}")
    fun deleteMessage(@PathVariable messageId: Long): ResponseEntity<MessageResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        messageService.deleteMessage(messageId, userId)
        return ResponseEntity.ok(MessageResponse(success = true, message = "Message deleted successfully"))
    }
}
