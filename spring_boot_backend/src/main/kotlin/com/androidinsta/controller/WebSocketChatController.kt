package com.androidinsta.controller

import com.androidinsta.Model.MessageType
import com.androidinsta.Service.MessageService
import com.androidinsta.dto.MessageDto
import com.androidinsta.dto.SendMessageRequest
import com.androidinsta.dto.toDto
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class WebSocketChatController(
    private val messageService: MessageService
) {
    
    /**
     * Nhận message từ client qua WebSocket
     * Client gửi đến: /app/chat
     * Response gửi về: /user/{username}/queue/messages
     */
    @MessageMapping("/chat")
    @SendToUser("/queue/messages")
    fun handleChatMessage(
        @Payload request: SendMessageRequest,
        headerAccessor: SimpMessageHeaderAccessor,
        principal: Principal?
    ): MessageDto {
        // Get sender ID from principal (JWT authentication)
        val senderId = principal?.name?.toLongOrNull() 
            ?: throw RuntimeException("User not authenticated")
        
        val messageType = try {
            MessageType.valueOf(request.messageType.lowercase())
        } catch (e: Exception) {
            MessageType.text
        }
        
        val message = messageService.sendMessage(
            senderId = senderId,
            receiverId = request.receiverId,
            content = request.content,
            mediaUrl = request.mediaUrl,
            messageType = messageType
        )
        
        return message.toDto()
    }
    
    /**
     * Typing indicator
     * Client gửi: /app/typing
     */
    @MessageMapping("/typing")
    fun handleTyping(
        @Payload data: TypingIndicator,
        principal: Principal?
    ) {
        // Broadcast typing indicator to receiver
        // Implementation depends on requirements
    }
}

data class TypingIndicator(
    val receiverId: Long,
    val isTyping: Boolean
)
