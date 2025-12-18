package com.androidinsta.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthChannelInterceptor(
    private val jwtUtil: JwtUtil
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
        
        // Log ALL STOMP commands to debug routing issues
        println("🔍 WS Interceptor: Command=${accessor?.command}, Destination=${accessor?.destination}")

        if (StompCommand.CONNECT == accessor?.command) {
            val authHeader = accessor.getFirstNativeHeader("Authorization")
            println("WS Interceptor: Connecting... AuthHeader present? ${authHeader != null}")
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)
                try {
                    if (jwtUtil.validateToken(token)) {
                        val userId = jwtUtil.getUserIdFromToken(token)
                        println("WS Interceptor: Token valid. UserId: $userId")
                        
                        // Create Principal for WebSocket session
                        val authentication = UsernamePasswordAuthenticationToken(
                            userId.toString(), // Principal Name
                            null,
                            emptyList() // Authorities
                        )
                        
                        accessor.user = authentication
                        SecurityContextHolder.getContext().authentication = authentication
                        println("WS Interceptor: Authentication set for user $userId")
                    } else {
                        println("WS Interceptor: Token invalid")
                    }
                } catch (e: Exception) {
                    println("WebSocket Auth Error: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                 println("WS Interceptor: No Bearer token found in CONNECT header")
            }
        }
        
        // Log message payload for SEND commands
        if (StompCommand.SEND == accessor?.command) {
            println("📤 WS Interceptor: SEND to ${accessor.destination}, Payload: ${String(message.payload as ByteArray)}")
        }
        
        return message
    }
}
