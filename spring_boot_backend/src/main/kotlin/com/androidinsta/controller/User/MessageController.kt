package com.androidinsta.controller.User

import com.androidinsta.Service.MessageService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import com.androidinsta.Model.MessageType
import com.androidinsta.Repository.User.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = ["*"])
class MessageController(
    private val messageService: MessageService,
    private val userRepository: UserRepository
) {

    /**
     * POST /api/messages - Gửi tin nhắn
     */
    @PostMapping
    fun sendMessage(@RequestBody request: SendMessageRequest): ResponseEntity<*> {
        return try {
            val senderId = SecurityUtil.getCurrentUserId()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "Unauthorized"
                    )
                )

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

            ResponseEntity.status(HttpStatus.CREATED).body(
                mapOf(
                    "success" to true,
                    "message" to "Message sent successfully",
                    "data" to message.toDto()
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Failed to send message")
                )
            )
        }
    }

    /**
     * GET /api/messages/conversations - Lấy danh sách conversations
     */
    @GetMapping("/conversations")
    fun getConversations(): ResponseEntity<*> {
        return try {
            val userId = SecurityUtil.getCurrentUserId()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "Unauthorized"
                    )
                )

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
                    unreadCount = unreadCount
                )
            }

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "data" to conversationDtos
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Failed to get conversations")
                )
            )
        }
    }

    /**
     * GET /api/messages/chat/{partnerId} - Lấy lịch sử chat với một user
     */
    @GetMapping("/chat/{partnerId}")
    fun getChatHistory(
        @PathVariable partnerId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<*> {
        return try {
            val userId = SecurityUtil.getCurrentUserId()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "Unauthorized"
                    )
                )

            val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
            val messagesPage = messageService.getChatHistory(userId, partnerId, pageable)
            
            val messageDtos = messagesPage.content.map { it.toDto() }

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "data" to mapOf(
                        "messages" to messageDtos,
                        "currentPage" to messagesPage.number,
                        "totalPages" to messagesPage.totalPages,
                        "totalItems" to messagesPage.totalElements
                    )
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Failed to get chat history")
                )
            )
        }
    }

    /**
     * GET /api/messages/unread/{partnerId} - Đếm tin nhắn chưa đọc từ một user
     */
    @GetMapping("/unread/{partnerId}")
    fun getUnreadCount(@PathVariable partnerId: Long): ResponseEntity<*> {
        return try {
            val userId = SecurityUtil.getCurrentUserId()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "Unauthorized"
                    )
                )

            val count = messageService.countUnreadMessages(userId, partnerId)

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "data" to mapOf("count" to count)
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Failed to get unread count")
                )
            )
        }
    }

    /**
     * PUT /api/messages/read/{partnerId} - Đánh dấu tất cả tin nhắn từ một user là đã đọc
     */
    @PutMapping("/read/{partnerId}")
    fun markAsRead(@PathVariable partnerId: Long): ResponseEntity<*> {
        return try {
            val userId = SecurityUtil.getCurrentUserId()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "Unauthorized"
                    )
                )

            messageService.markAsRead(userId, partnerId)

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Messages marked as read"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Failed to mark messages as read")
                )
            )
        }
    }

    /**
     * DELETE /api/messages/{messageId} - Xóa tin nhắn
     */
    @DeleteMapping("/{messageId}")
    fun deleteMessage(@PathVariable messageId: Long): ResponseEntity<*> {
        return try {
            val userId = SecurityUtil.getCurrentUserId()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "Unauthorized"
                    )
                )

            messageService.deleteMessage(messageId, userId)

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Message deleted successfully"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Failed to delete message")
                )
            )
        }
    }
}
