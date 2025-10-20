package com.androidinsta.Model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 50)
    val username: String,

    @Column(nullable = false, unique = true, length = 100)
    val email: String,

    @Column(nullable = false, length = 255)
    val password: String,

    @Column(name = "full_name", length = 100)
    val fullName: String? = null,

    @Column(columnDefinition = "TEXT")
    val bio: String? = null,

    @Column(name = "avatar_url", length = 255)
    val avatarUrl: String? = null,

    @Column(name = "is_verified")
    val isVerified: Boolean = false,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    val role: Role? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    // Relationships
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val posts: List<Post> = emptyList(),

    @OneToMany(mappedBy = "follower", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val following: List<Follow> = emptyList(),

    @OneToMany(mappedBy = "followed", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val followers: List<Follow> = emptyList(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val likes: List<Like> = emptyList(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val comments: List<Comment> = emptyList(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val savedPosts: List<SavedPost> = emptyList()
)
