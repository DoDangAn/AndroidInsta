package com.androidinsta.Model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "friend_requests",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["sender_id", "receiver_id"])
    ]
)
data class FriendRequest(
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
    var status: FriendRequestStatus = FriendRequestStatus.PENDING,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var respondedAt: LocalDateTime? = null
)

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}
