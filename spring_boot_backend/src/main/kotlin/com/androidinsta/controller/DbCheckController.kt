package com.androidinsta.controller

import com.androidinsta.Repository.User.MessageRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/db")
class DbCheckController(
    private val messageRepository: MessageRepository
) {

    @GetMapping("/stats")
    fun getStats(): Map<String, Any> {
        return try {
            val count = messageRepository.count()
            // Simplified safe mapping
            val lastMessages = messageRepository.findAll()
                .map { msg ->
                     mapOf(
                         "id" to msg.id,
                         "content" to (msg.content ?: "null"),
                         "senderId" to msg.sender.id,
                         "receiverId" to msg.receiver.id
                     )
                }
                .takeLast(5)

            mapOf(
                "total_messages" to count,
                "data" to lastMessages
            )
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: "Unknown error"))
        }
    }
}
