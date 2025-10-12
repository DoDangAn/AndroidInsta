package com.androidinsta.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Post(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    val user: User, // User class đã import ở trên

    var caption: String? = null,

    @Enumerated(EnumType.STRING)
    var visibility: Visibility = Visibility.PUBLIC,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null
)
