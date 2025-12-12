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
        // DON'T cache AdminOverviewStatsDto - complex DTO, query is fast
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
        
        // DON'T cache AdminOverallStatsDto - complex DTO, query is fast with DB indexes
        return stats
    }

    /**
     * Get user statistics over time
     */
    fun getUserStats(period: String): AdminTimeStatsDto {
        val days = parsePeriod(period)
        val startDate = LocalDateTime.now().minusDays(days.toLong())
        
        val usersByDay = userRepository.findByCreatedAtAfter(startDate)
            .filter { it.createdAt != null }
            .groupBy { it.createdAt!!.toLocalDate() }
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
            .filter { it.createdAt != null }
            .groupBy { it.createdAt!!.toLocalDate() }
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
    fun getEngagementStats(period: String): EngagementStatsData {
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
        
        return EngagementStatsData(
            likes = AdminTimeStatsDto(period, likesByDay),
            comments = AdminTimeStatsDto(period, commentsByDay),
            follows = AdminTimeStatsDto(period, followsByDay)
        )
    }

    /**
     * Get top users (most followers, posts, or likes)
     * DON'T cache List<AdminTopUserDto> - complex DTO, query is fast with DB indexes
     */
    fun getTopUsers(type: String, limit: Int): List<AdminTopUserDto> {
        return getTopUsersInternal(type, limit)
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
                            count = user.posts.size.toLong(),
                            type = "posts"
                        )
                    }
                    .sortedByDescending { it.count }
                    .take(limit)
            }
            "likes" -> {
                userRepository.findAll()
                    .map { user ->
                        val totalLikes = user.posts.sumOf { it.likes.size.toLong() }
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
        // DON'T cache List<AdminTopPostDto> - complex DTO, query is fast with DB indexes
        val posts = postRepository.findAll()
        
        val result = when (type.lowercase()) {
            "likes" -> {
                posts.sortedByDescending { it.likes.size }
                    .take(limit)
                    .map { post ->
                        AdminTopPostDto(
                            id = post.id,
                            caption = post.caption,
                            username = post.user.username,
                            userId = post.user.id,
                            likesCount = post.likes.size.toLong(),
                            commentsCount = post.comments.size.toLong(),
                            createdAt = post.createdAt ?: java.time.LocalDateTime.now()
                        )
                    }
            }
            "comments" -> {
                posts.sortedByDescending { it.comments.size }
                    .take(limit)
                    .map { post ->
                        AdminTopPostDto(
                            id = post.id,
                            caption = post.caption,
                            username = post.user.username,
                            userId = post.user.id,
                            likesCount = post.likes.size.toLong(),
                            commentsCount = post.comments.size.toLong(),
                            createdAt = post.createdAt ?: java.time.LocalDateTime.now()
                        )
                    }
            }
            else -> emptyList()
        }
        
        // DON'T cache List<AdminTopPostDto> - complex DTO, query is fast with DB indexes
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
