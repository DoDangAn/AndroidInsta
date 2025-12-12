package com.androidinsta.Model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class NotificationType {
    LIKE,       // Ai đó thích bài viết của bạn
    COMMENT,    // Ai đó comment vào bài viết của bạn
    FOLLOW,     // Ai đó follow bạn
    MESSAGE,    // Ai đó gửi tin nhắn cho bạn
    NEW_POST    // Người bạn follow đăng bài mới
}

@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('LIKE', 'COMMENT', 'FOLLOW', 'MESSAGE', 'NEW_POST')")
    val type: NotificationType,

    @Column(name = "entity_id")
    val entityId: Long? = null,

    @Column(name = "is_read", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    var isRead: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)