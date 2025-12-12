package com.androidinsta.Model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class MessageType {
    text, image, video
}

@Entity
@Table(name = "messages")
data class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,

    @Column(columnDefinition = "TEXT")
    val content: String? = null,

    @Column(name = "media_url", length = 255)
    val mediaUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, columnDefinition = "ENUM('text', 'image', 'video') DEFAULT 'text'")
    val messageType: MessageType = MessageType.text,

    @Column(name = "is_read", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    val isRead: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)