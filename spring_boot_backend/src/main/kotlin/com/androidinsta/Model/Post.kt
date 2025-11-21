package com.androidinsta.Model

import com.androidinsta.config.MediaTypeDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.persistence.*
import java.time.LocalDateTime

enum class Visibility {
    PUBLIC,    // Hiển thị công khai
    PRIVATE,   // Chỉ mình tôi xem
    ADVERTISE  // Quảng cáo - hiển thị cho tất cả users
}

@JsonDeserialize(using = MediaTypeDeserializer::class)
enum class MediaType {
    IMAGE, VIDEO
}

@Embeddable
data class MediaFile(
    @Column(name = "file_url", nullable = false, length = 255)
    val fileUrl: String,

    @Convert(converter = com.androidinsta.config.MediaTypeConverter::class)
    @Column(name = "file_type", nullable = false, columnDefinition = "ENUM('IMAGE', 'VIDEO') DEFAULT 'IMAGE'")
    val fileType: MediaType = MediaType.IMAGE,

    @Column(name = "order_index", nullable = false, columnDefinition = "INT DEFAULT 1")
    val orderIndex: Int = 1,

    @Column(name = "cloudinary_public_id", length = 255)
    val cloudinaryPublicId: String? = null,

    @Column(name = "duration")
    val duration: Int? = null,

    @Column(name = "thumbnail_url", length = 500)
    val thumbnailUrl: String? = null
)

@Entity
@Table(name = "posts")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(columnDefinition = "TEXT")
    val caption: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('PUBLIC', 'PRIVATE', 'ADVERTISE') DEFAULT 'PUBLIC'")
    val visibility: Visibility = Visibility.PUBLIC,


    @ElementCollection
    @CollectionTable(
        name = "media_files",
        joinColumns = [JoinColumn(name = "post_id")]
    )
    val mediaFiles: List<MediaFile> = emptyList(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @Column(name = "updated_at", insertable = false)
    var updatedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val comments: MutableList<Comment> = mutableListOf(),

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val likes: MutableSet<Like> = mutableSetOf(),

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val savedPosts: MutableSet<SavedPost> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "post_tags",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    val tags: MutableSet<Tag> = mutableSetOf()
) {

    @PrePersist
    fun onPrePersist() {
        createdAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Post
        if (id == 0L && other.id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
