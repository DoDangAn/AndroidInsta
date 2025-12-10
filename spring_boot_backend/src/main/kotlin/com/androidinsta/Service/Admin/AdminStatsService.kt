package com.androidinsta.Service.Admin

import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.LikeRepository
import com.androidinsta.Repository.User.CommentRepository
import com.androidinsta.Repository.User.FollowRepository
import com.androidinsta.dto.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class AdminStatsService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository,
    private val followRepository: FollowRepository,
    private val redisService: com.androidinsta.Service.RedisService
) {

    /**
     * Get overall system statistics with caching
     */
    fun getOverviewStats(): AdminOverviewStatsDto {
        val cacheKey = "admin:stats:overview"
        val cached = redisService.get(cacheKey)
        
        if (cached != null && cached is AdminOverviewStatsDto) {
            return cached
        }
        
        val totalUsers = userRepository.count()
        val activeUsers = userRepository.countByIsActive(true)
        val verifiedUsers = userRepository.countByIsVerified(true)
        val totalPosts = postRepository.count()
        val totalLikes = likeRepository.count()
        val totalComments = commentRepository.count()
        val totalFollows = followRepository.count()
        
        val today = LocalDateTime.now().toLocalDate().atStartOfDay()
        val newUsersToday = userRepository.countByCreatedAtAfter(today)
        val newPostsToday = postRepository.countByCreatedAtAfter(today)
        
        val stats = AdminOverviewStatsDto(
            totalUsers = totalUsers,
            activeUsers = activeUsers,
            verifiedUsers = verifiedUsers,
            totalPosts = totalPosts,
            totalLikes = totalLikes,
            totalComments = totalComments,
            totalFollows = totalFollows,
            newUsersToday = newUsersToday,
            newPostsToday = newPostsToday
        )
        
        // Cache for 5 minutes - stats change frequently
        redisService.set(cacheKey, stats, java.time.Duration.ofMinutes(5))
        
        return stats
    }

    /**
     * Get user statistics over time
     */
    fun getUserStats(period: String): AdminTimeStatsDto {
        val days = parsePeriod(period)
        val startDate = LocalDateTime.now().minusDays(days.toLong())
        
        val usersByDay = userRepository.findByCreatedAtAfter(startDate)
            .groupBy { it.createdAt.toLocalDate() }
            .map { (date, users) ->
                AdminTimeDataPoint(
                    date = date.toString(),
                    count = users.size.toLong()
                )
            }
            .sortedBy { it.date }
        
        return AdminTimeStatsDto(
            period = period,
            data = usersByDay
        )
    }

    /**
     * Get post statistics over time
     */
    fun getPostStats(period: String): AdminTimeStatsDto {
        val days = parsePeriod(period)
        val startDate = LocalDateTime.now().minusDays(days.toLong())
        
        val postsByDay = postRepository.findByCreatedAtAfter(startDate)
            .groupBy { it.createdAt.toLocalDate() }
            .map { (date, posts) ->
                AdminTimeDataPoint(
                    date = date.toString(),
                    count = posts.size.toLong()
                )
            }
            .sortedBy { it.date }
        
        return AdminTimeStatsDto(
            period = period,
            data = postsByDay
        )
    }

    /**
     * Get engagement statistics (likes, comments, follows)
     */
    fun getEngagementStats(period: String): Map<String, AdminTimeStatsDto> {
        val days = parsePeriod(period)
        val startDate = LocalDateTime.now().minusDays(days.toLong())
        
        val likesByDay = likeRepository.findByCreatedAtAfter(startDate)
            .groupBy { it.createdAt.toLocalDate() }
            .map { (date, likes) ->
                AdminTimeDataPoint(date = date.toString(), count = likes.size.toLong())
            }
            .sortedBy { it.date }
        
        val commentsByDay = commentRepository.findByCreatedAtAfter(startDate)
            .groupBy { it.createdAt.toLocalDate() }
            .map { (date, comments) ->
                AdminTimeDataPoint(date = date.toString(), count = comments.size.toLong())
            }
            .sortedBy { it.date }
        
        val followsByDay = followRepository.findByCreatedAtAfter(startDate)
            .groupBy { it.createdAt.toLocalDate() }
            .map { (date, follows) ->
                AdminTimeDataPoint(date = date.toString(), count = follows.size.toLong())
            }
            .sortedBy { it.date }
        
        return mapOf(
            "likes" to AdminTimeStatsDto(period, likesByDay),
            "comments" to AdminTimeStatsDto(period, commentsByDay),
            "follows" to AdminTimeStatsDto(period, followsByDay)
        )
    }

    /**
     * Get top users (most followers, posts, or likes) with caching
     */
    fun getTopUsers(type: String, limit: Int): List<AdminTopUserDto> {
        val cacheKey = "admin:top:users:$type:$limit"
        val cached = redisService.get(cacheKey)
        
        @Suppress("UNCHECKED_CAST")
        if (cached != null && cached is List<*>) {
            return cached as List<AdminTopUserDto>
        }
        
        val result = getTopUsersInternal(type, limit)
        
        // Cache for 10 minutes
        redisService.set(cacheKey, result, java.time.Duration.ofMinutes(10))
        
        return result
    }
    
    private fun getTopUsersInternal(type: String, limit: Int): List<AdminTopUserDto> {
        return when (type.lowercase()) {
            "followers" -> {
                userRepository.findAll()
                    .map { user ->
                        AdminTopUserDto(
                            id = user.id,
                            username = user.username,
                            fullName = user.fullName,
                            avatarUrl = user.avatarUrl,
                            isVerified = user.isVerified,
                            count = followRepository.countByFollowedId(user.id),
                            type = "followers"
                        )
                    }
                    .sortedByDescending { it.count }
                    .take(limit)
            }
            "posts" -> {
                userRepository.findAll()
                    .map { user ->
                        AdminTopUserDto(
                            id = user.id,
                            username = user.username,
                            fullName = user.fullName,
                            avatarUrl = user.avatarUrl,
                            isVerified = user.isVerified,
                            count = postRepository.countByUserId(user.id),
                            type = "posts"
                        )
                    }
                    .sortedByDescending { it.count }
                    .take(limit)
            }
            "likes" -> {
                userRepository.findAll()
                    .map { user ->
                        val userPosts = postRepository.findByUserId(user.id)
                        val totalLikes = userPosts.sumOf { it.likesCount.toLong() }
                        AdminTopUserDto(
                            id = user.id,
                            username = user.username,
                            fullName = user.fullName,
                            avatarUrl = user.avatarUrl,
                            isVerified = user.isVerified,
                            count = totalLikes,
                            type = "likes"
                        )
                    }
                    .sortedByDescending { it.count }
                    .take(limit)
            }
            else -> emptyList()
        }
    }

    /**
     * Get top posts (most liked or commented) with caching
     */
    fun getTopPosts(type: String, limit: Int): List<AdminTopPostDto> {
        val cacheKey = "admin:top:posts:$type:$limit"
        val cached = redisService.get(cacheKey)
        
        @Suppress("UNCHECKED_CAST")
        if (cached != null && cached is List<*>) {
            return cached as List<AdminTopPostDto>
        }
        
        val posts = postRepository.findAll()
        
        return when (type.lowercase()) {
            "likes" -> {
                posts.sortedByDescending { it.likesCount }
                    .take(limit)
                    .map { post ->
                        AdminTopPostDto(
                            id = post.id,
                            caption = post.caption,
                            username = post.user.username,
                            userId = post.user.id,
                            likesCount = post.likesCount,
                            commentsCount = post.commentsCount,
                            createdAt = post.createdAt
                        )
                    }
            }
            "comments" -> {
                posts.sortedByDescending { it.commentsCount }
                    .take(limit)
                    .map { post ->
                        AdminTopPostDto(
                            id = post.id,
                            caption = post.caption,
                            username = post.user.username,
                            userId = post.user.id,
                            likesCount = post.likesCount,
                            commentsCount = post.commentsCount,
                            createdAt = post.createdAt
                        )
                    }
            }
            else -> emptyList()
        }
        
        // Cache for 10 minutes
        redisService.set(cacheKey, result, java.time.Duration.ofMinutes(10))
        
        return result
    }

    /**
     * Parse period string (e.g., "7d", "30d", "1y") to number of days
     */
    private fun parsePeriod(period: String): Int {
        val amount = period.dropLast(1).toIntOrNull() ?: 7
        val unit = period.last()
        
        return when (unit.lowercaseChar()) {
            'd' -> amount
            'w' -> amount * 7
            'm' -> amount * 30
            'y' -> amount * 365
            else -> 7
        }
    }
}
