package com.androidinsta.controller

import com.androidinsta.Model.MessageType
import com.androidinsta.Service.MessageService
import com.androidinsta.dto.MessageDto
import com.androidinsta.dto.SendMessageRequest
import com.androidinsta.dto.toDto
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import java.security.Principal

/**
 * WebSocket controller cho real-time chat
 * Handles WebSocket connections for instant messaging
 * Client connects via STOMP over WebSocket
 */
@Controller
class WebSocketChatController(
    private val messageService: MessageService,
    private val messagingTemplate: SimpMessagingTemplate
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
        val senderId = principal?.name?.toLongOrNull() 
            ?: throw IllegalStateException("User not authenticated")
        
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
     * Server broadcast đến receiver: /user/{receiverId}/queue/typing
     */
    @MessageMapping("/typing")
    fun handleTyping(
        @Payload data: TypingIndicator,
        headerAccessor: SimpMessageHeaderAccessor,
        principal: Principal?
    ) {
        val senderId = principal?.name?.toLongOrNull() 
            ?: throw IllegalStateException("User not authenticated")
        
        // Send typing indicator to specific receiver
        messagingTemplate.convertAndSendToUser(
            data.receiverId.toString(),
            "/queue/typing",
            TypingIndicatorResponse(
                senderId = senderId,
                isTyping = data.isTyping
            )
        )
    }
}

/**
 * Data class for typing indicator events (request from client)
 */
data class TypingIndicator(
    val receiverId: Long,
    val isTyping: Boolean
)

/**
 * Data class for typing indicator response (sent to receiver)
 */
data class TypingIndicatorResponse(
    val senderId: Long,
    val isTyping: Boolean
)
