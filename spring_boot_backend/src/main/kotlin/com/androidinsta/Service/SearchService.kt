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
     * Tìm kiếm users theo keyword
     * 
     * Search Strategy:
     * - Tìm kiếm trong username, fullName, email (case-insensitive)
     * - Sắp xếp: Exact match trước, sau đó theo số followers
     * - Cache kết quả 5 phút để tăng performance
     * 
     * @param keyword Từ khóa tìm kiếm (trim và lowercase)
     * @param pageable Pagination parameters
     * @param currentUserId User hiện tại để check follow status (optional)
     * @return Page of UserSearchResult với follow status
     */
    fun searchUsers(keyword: String, pageable: Pageable, currentUserId: Long? = null): Page<UserSearchResult> {
        // Use DB-side paged query for users to avoid loading all results into memory
        val usersPage = userRepository.searchUsersPaged(keyword, pageable)

        val mapped = usersPage.map { user ->
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

        return mapped
    }

    /**
     * Tìm kiếm posts theo keyword
     * 
     * Search Strategy:
     * - Tìm trong caption và username của chủ post
     * - Chỉ hiển thị PUBLIC posts (không search PRIVATE)
     * - Sắp xếp theo thời gian tạo (mới nhất trước)
     * - Include like count và comment count
     * 
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Pagination với sort theo createdAt DESC
     * @return Page of PostSearchResult
     */
    fun searchPosts(keyword: String, pageable: Pageable): Page<PostSearchResult> {
        return postRepository.searchPosts(keyword, pageable)
            .map { post -> toPostSearchResult(post) }
    }

    /**
     * Tìm kiếm reels (video posts only)
     * 
     * Search Strategy:
     * - Tìm posts giống searchPosts
     * - Lọc chỉ lấy posts có ít nhất 1 video file
     * - Phù hợp cho Instagram Reels feature
     * 
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Pagination parameters
     * @return Page of PostSearchResult (filtered for videos)
     */
    fun searchReels(keyword: String, pageable: Pageable): Page<PostSearchResult> {
        // Use a DB-side query that only returns posts with video media
        return postRepository.searchReels(keyword, pageable)
            .map { post -> toPostSearchResult(post) }
    }

    /**
     * Tìm kiếm hashtags theo keyword
     * 
     * Search Strategy:
     * - Tìm kiếm trong tag name (case-insensitive)
     * - Sắp xếp theo tên (alphabet)
     * - Bao gồm số lượng posts sử dụng tag
     * 
     * @param keyword Từ khóa tìm kiếm (không cần dấu #)
     * @param pageable Pagination với sort theo name ASC
     * @return Page of TagSearchResult
     */
    fun searchTags(keyword: String, pageable: Pageable): Page<TagSearchResult> {
        return tagRepository.searchTags(keyword, pageable)
            .map { tag -> toTagSearchResult(tag) }
    }

    /**
     * Tìm kiếm tổng hợp (all categories)
     * 
     * Use Case:
     * - Hiển thị kết quả nhanh khi user gõ search query
     * - Top 10 kết quả mỗi loại (users, posts, tags)
     * - Không cần pagination vì chỉ lấy preview
     * 
     * @param keyword Từ khóa tìm kiếm
     * @return SearchAllResult chứa top results của mỗi category
     */
    fun searchAll(keyword: String): SearchAllResult {
        val pageable = PageRequest.of(0, 10)

        val cacheKey = "search:all:preview:${'$'}{keyword.trim().lowercase()}"
        val cached = redisService.get(cacheKey, SearchAllResult::class.java)
        if (cached != null) return cached

        val users = searchUsers(keyword, pageable).content
        val posts = searchPosts(keyword, pageable).content
        val tags = searchTags(keyword, pageable).content

        val result = SearchAllResult(
            users = users,
            posts = posts,
            tags = tags
        )

        // Cache short-lived preview
        redisService.set(cacheKey, result, java.time.Duration.ofSeconds(60))
        return result
    }

    /**
     * Lấy danh sách trending hashtags
     * 
     * Trending Logic:
     * - Sắp xếp theo số lượng posts sử dụng tag (DESC)
     * - Hiển thị tags phổ biến nhất hiện tại
     * - Phù hợp cho Explore/Discover feature
     * 
     * @param pageable Pagination parameters
     * @return Page of TagSearchResult sorted by post count
     */
    fun getTrendingTags(pageable: Pageable): Page<TagSearchResult> {
        val key = "search:trending:tags:page:${'$'}{pageable.pageNumber}:size:${'$'}{pageable.pageSize}"
        // Unable to deserialize Page generics easily; use simple cache for list
        val cachedList = redisService.get(key + ":list", List::class.java) as? List<TagSearchResult>
        if (!cachedList.isNullOrEmpty()) {
            return org.springframework.data.domain.PageImpl(cachedList, pageable, cachedList.size.toLong())
        }

        val page = tagRepository.findTrendingTags(pageable).map { tag -> toTagSearchResult(tag) }
        // Cache list form
        redisService.set(key + ":list", page.content, java.time.Duration.ofMinutes(5))
        return page
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
