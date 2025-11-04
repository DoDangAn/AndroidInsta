package com.androidinsta.Model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class NotificationType {
    LIKE,           // Ai đó thích bài viết/reel của bạn
    COMMENT,        // Ai đó comment vào bài viết của bạn
    FOLLOW,         // Ai đó follow bạn
    MESSAGE,        // Ai đó gửi tin nhắn cho bạn
    POST,           // Bạn bè đăng bài viết mới
    REPLY,          // Ai đó trả lời comment của bạn
    MENTION,        // Ai đó mention bạn
    FRIEND_REQUEST, // Ai đó gửi lời mời kết bạn
    FRIEND_ACCEPT   // Ai đó chấp nhận lời mời kết bạn của bạn
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
    @Column(nullable = false)
    val type: NotificationType,

    @Column(name = "entity_id")
    val entityId: Long? = null,

    @Column(columnDefinition = "TEXT")
    val message: String? = null,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)