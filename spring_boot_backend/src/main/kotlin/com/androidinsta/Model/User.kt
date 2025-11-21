package com.androidinsta.Model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 50)
    val username: String,

    @Column(nullable = false, unique = true, length = 100)
    @JsonIgnore
    val email: String,

    @Column(nullable = false, length = 255)
    @JsonIgnore
    val password: String,

    @Column(name = "full_name", length = 100)
    val fullName: String? = null,

    @Column(columnDefinition = "TEXT")
    val bio: String? = null,

    @Column(name = "avatar_url", length = 255)
    val avatarUrl: String? = null,

    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    val isVerified: Boolean = false,

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    val isActive: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @JsonIgnore
    val role: Role? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", insertable = false)
    val updatedAt: LocalDateTime? = null,

    // Relationships - Ignore when serializing to JSON to avoid infinite recursion
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val posts: List<Post> = emptyList(),

    @OneToMany(mappedBy = "follower", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val following: List<Follow> = emptyList(),

    @OneToMany(mappedBy = "followed", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val followers: List<Follow> = emptyList(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val likes: List<Like> = emptyList(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val comments: List<Comment> = emptyList(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val savedPosts: List<SavedPost> = emptyList()
)
