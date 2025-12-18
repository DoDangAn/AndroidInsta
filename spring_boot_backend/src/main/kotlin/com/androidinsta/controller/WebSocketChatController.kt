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
     * Real-time: Gửi đến CẢ NGƯỜI GỬI & NGƯỜI NHẬN ngay lập tức
     */
    @MessageMapping("/chat")
    fun handleChatMessage(
        @Payload rawPayload: String,
        headerAccessor: SimpMessageHeaderAccessor,
        principal: Principal?
    ) {
        println("WS Controller: RAW JSON received: $rawPayload")
        
        val senderId = principal?.name?.toLongOrNull()
            ?: throw IllegalStateException("User not authenticated")
            
        // Parse JSON manually
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        val jsonNode = mapper.readTree(rawPayload)
        
        val receiverId = jsonNode.get("receiverId")?.asLong() 
            ?: throw IllegalArgumentException("Missing receiverId")
            
        val content = jsonNode.get("content")?.asText()
        val mediaUrl = jsonNode.get("mediaUrl")?.asText()
        
        val messageTypeStr = jsonNode.get("messageType")?.asText() ?: "text"
        val messageType = try {
            MessageType.valueOf(messageTypeStr.lowercase())
        } catch (e: Exception) {
            MessageType.text
        }
        
        println("WS Controller: Parsed - receiverId=$receiverId, content=$content, type=$messageType")
        
        println("WS Controller: Saving message...")
        val message = messageService.sendMessage(
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            mediaUrl = mediaUrl,
            messageType = messageType
        )
        println("WS Controller: Message saved with ID ${message.id}")
        
        val messageDto = message.toDto()

        messagingTemplate.convertAndSendToUser(
            receiverId.toString(),
            "/queue/messages",
            messageDto
        )

        // ✅ Gửi tin nhắn đến NGƯỜI GỬI (confirmation)
        messagingTemplate.convertAndSendToUser(
            senderId.toString(),
            "/queue/messages",
            messageDto
        )
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
    
    /**
     * Exception handler for WebSocket errors
     */
    @org.springframework.messaging.handler.annotation.MessageExceptionHandler
    fun handleException(exception: Exception): String {
        println("❌ WebSocket Error: ${exception.javaClass.simpleName}: ${exception.message}")
        exception.printStackTrace()
        return "Error: ${exception.message}"
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

