package com.androidinsta.Model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "friendships",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "friend_id"])
    ]
)
data class Friendship(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    val friend: User,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
