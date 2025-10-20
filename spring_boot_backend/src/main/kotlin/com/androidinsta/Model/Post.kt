package com.androidinsta.Model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class Visibility {
    PUBLIC,    // Hiển thị công khai
    PRIVATE,   // Chỉ mình tôi xem
    ADVERTISE  // Quảng cáo - hiển thị cho tất cả users
}

enum class MediaType {
    IMAGE, VIDEO
}

@Embeddable
data class MediaFile(
    @Column(name = "file_url", nullable = false, length = 255)
    val fileUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    val fileType: MediaType = MediaType.IMAGE,

    @Column(name = "order_index", nullable = false)
    val orderIndex: Int = 1
)

@Entity
@Table(name = "posts")
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(columnDefinition = "TEXT")
    val caption: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val visibility: Visibility = Visibility.PUBLIC,


    @ElementCollection
    @CollectionTable(
        name = "media_files",
        joinColumns = [JoinColumn(name = "post_id")]
    )
    val mediaFiles: List<MediaFile> = emptyList(),

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val comments: List<Comment> = emptyList(),

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val likes: List<Like> = emptyList(),

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val savedPosts: List<SavedPost> = emptyList(),

    @ManyToMany
    @JoinTable(
        name = "post_tags",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    val tags: List<Tag> = emptyList()
)
 
