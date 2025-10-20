package com.androidinsta.Model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tags")
data class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 50)
    val name: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // Relationships
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    val posts: List<Post> = emptyList()
)