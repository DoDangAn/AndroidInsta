package com.androidinsta.Service

import com.androidinsta.Model.MediaType
import com.androidinsta.Model.Post
import com.androidinsta.Model.Tag
import com.androidinsta.Model.User
import com.androidinsta.Repository.User.*
import com.androidinsta.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SearchService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val tagRepository: TagRepository,
    private val followRepository: FollowRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository,
    private val redisService: RedisService
) {

    /**
     * Tìm kiếm users
     */
    fun searchUsers(keyword: String, pageable: Pageable, currentUserId: Long? = null): Page<UserSearchResult> {
        val cacheKey = "search:users:$keyword:${pageable.pageNumber}:${currentUserId ?: "guest"}"
        
        val cached = redisService.get(cacheKey, Page::class.java)
        if (cached != null) {
            @Suppress("UNCHECKED_CAST")
            return cached as Page<UserSearchResult>
        }
        
        val users = userRepository.searchUsers(keyword)
        
        val results = users.map { user ->
            val followersCount = followRepository.countByFollowedId(user.id)
            val isFollowing = if (currentUserId != null && currentUserId != user.id) {
                followRepository.existsByFollowerIdAndFollowedId(currentUserId, user.id)
            } else {
                false
            }
            
            UserSearchResult(
                id = user.id,
                username = user.username,
                fullName = user.fullName,
                avatarUrl = user.avatarUrl,
                isVerified = user.isVerified,
                followersCount = followersCount,
                isFollowing = isFollowing
            )
        }
        
        // Manual pagination vì searchUsers trả về List
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, results.size)
        val pageContent = if (start < results.size) results.subList(start, end) else emptyList()
        
        val result = org.springframework.data.domain.PageImpl(
            pageContent,
            pageable,
            results.size.toLong()
        )
        
        // Cache result for 5 minutes
        redisService.set(cacheKey, result, java.time.Duration.ofMinutes(5))
        
        return result
    }

    /**
     * Tìm kiếm posts
     */
    fun searchPosts(keyword: String, pageable: Pageable): Page<PostSearchResult> {
        return postRepository.searchPosts(keyword, pageable)
            .map { post -> toPostSearchResult(post) }
    }

    /**
     * Tìm kiếm reels (video posts)
     */
    fun searchReels(keyword: String, pageable: Pageable): Page<PostSearchResult> {
        return postRepository.searchPosts(keyword, pageable)
            .map { post -> toPostSearchResult(post) }
            .map { it }
            .let { page ->
                // Filter only video posts
                val videoResults = page.content.filter { result ->
                    result.mediaFiles.any { it.fileType == MediaType.VIDEO }
                }
                org.springframework.data.domain.PageImpl(
                    videoResults,
                    pageable,
                    videoResults.size.toLong()
                )
            }
    }

    /**
     * Tìm kiếm tags
     */
    fun searchTags(keyword: String, pageable: Pageable): Page<TagSearchResult> {
        return tagRepository.searchTags(keyword, pageable)
            .map { tag -> toTagSearchResult(tag) }
    }

    /**
     * Tìm kiếm tổng hợp (all)
     */
    fun searchAll(keyword: String): SearchAllResult {
        val pageable = PageRequest.of(0, 10)
        
        val users = searchUsers(keyword, pageable).content
        val posts = searchPosts(keyword, pageable).content
        val tags = searchTags(keyword, pageable).content
        
        return SearchAllResult(
            users = users,
            posts = posts,
            tags = tags
        )
    }

    /**
     * Lấy trending tags
     */
    fun getTrendingTags(pageable: Pageable): Page<TagSearchResult> {
        return tagRepository.findTrendingTags(pageable)
            .map { tag -> toTagSearchResult(tag) }
    }

    // Helper methods
    private fun toPostSearchResult(post: Post): PostSearchResult {
        val likeCount = likeRepository.countByPostId(post.id)
        val commentCount = commentRepository.countByPostId(post.id)
        
        return PostSearchResult(
            id = post.id,
            userId = post.user.id,
            username = post.user.username,
            userAvatarUrl = post.user.avatarUrl,
            caption = post.caption,
            mediaFiles = post.mediaFiles.map { media ->
                MediaFileInfo(
                    fileUrl = media.fileUrl,
                    fileType = media.fileType,
                    thumbnailUrl = media.thumbnailUrl
                )
            },
            likeCount = likeCount,
            commentCount = commentCount,
            createdAt = post.createdAt
        )
    }

    private fun toTagSearchResult(tag: Tag): TagSearchResult {
        return TagSearchResult(
            id = tag.id,
            name = tag.name,
            postsCount = tag.posts.size.toLong()
        )
    }
}
