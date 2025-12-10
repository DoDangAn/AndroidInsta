package com.androidinsta.Service.Admin

import com.androidinsta.Model.User
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Repository.User.FollowRepository
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.LikeRepository
import com.androidinsta.Repository.User.CommentRepository
import com.androidinsta.dto.AdminUserStatsDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminUserService(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val postRepository: PostRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository,
    private val redisService: com.androidinsta.Service.RedisService,
    private val kafkaProducerService: com.androidinsta.Service.KafkaProducerService
) {

    /**
     * Get all users with search and pagination
     */
    fun getAllUsers(keyword: String, pageable: Pageable): Page<User> {
        return if (keyword.isBlank()) {
            userRepository.findAll(pageable)
        } else {
            userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword, keyword, pageable
            )
        }
    }

    /**
     * Get user by ID with caching
     */
    fun getUserById(userId: Long): User {
        // Kiểm tra cache trước
        val cachedUser = redisService.getCachedUser(userId)
        if (cachedUser != null && cachedUser is User) {
            return cachedUser
        }
        
        // Nếu không có trong cache, query từ DB
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found with id: $userId") }
        
        // Lưu vào cache (TTL: 60 phút)
        redisService.cacheUser(userId, user, 60)
        
        return user
    }

    /**
     * Ban a user (set isActive = false)
     */
    @Transactional
    fun banUser(userId: Long) {
        val user = getUserById(userId)
        userRepository.save(user.copy(isActive = false))
        
        // Send Kafka event for audit trail
        kafkaProducerService.sendNotificationEvent(
            userId = userId,
            title = "Account Banned",
            message = "User ${user.username} has been banned by admin",
            type = "USER_BANNED"
        )
        
        // Xóa cache sau khi update
        redisService.invalidateUserCache(userId)
    }

    /**
     * Unban a user (set isActive = true)
     */
    @Transactional
    fun unbanUser(userId: Long) {
        val user = getUserById(userId)
        userRepository.save(user.copy(isActive = true))
        
        // Send Kafka event for audit trail
        kafkaProducerService.sendNotificationEvent(
            userId = userId,
            title = "Account Unbanned",
            message = "User ${user.username} has been unbanned by admin",
            type = "USER_UNBANNED"
        )
        
        // Xóa cache sau khi update
        redisService.invalidateUserCache(userId)
    }

    /**
     * Verify a user (set isVerified = true)
     */
    @Transactional
    fun verifyUser(userId: Long) {
        val user = getUserById(userId)
        userRepository.save(user.copy(isVerified = true))
        
        // Send Kafka event - verification is important!
        kafkaProducerService.sendNotificationEvent(
            userId = userId,
            title = "Account Verified",
            message = "Congratulations! Your account ${user.username} has been verified",
            type = "USER_VERIFIED"
        )
        
        // Xóa cache sau khi update
        redisService.invalidateUserCache(userId)
    }

    /**
     * Unverify a user (set isVerified = false)
     */
    @Transactional
    fun unverifyUser(userId: Long) {
        val user = getUserById(userId)
        userRepository.save(user.copy(isVerified = false))
        
        // Send Kafka event
        kafkaProducerService.sendNotificationEvent(
            userId = userId,
            title = "Verification Removed",
            message = "Your verification status has been removed",
            type = "USER_UNVERIFIED"
        )
        
        // Xóa cache sau khi update
        redisService.invalidateUserCache(userId)
    }

    /**
     * Delete a user permanently
     */
    @Transactional
    fun deleteUser(userId: Long) {
        val user = getUserById(userId)
        
        // Send Kafka event BEFORE deleting - CRITICAL audit trail
        kafkaProducerService.sendNotificationEvent(
            userId = userId,
            title = "User Deleted",
            message = "User ${user.username} (ID: ${user.id}) has been permanently deleted by admin",
            type = "USER_DELETED"
        )
        
        userRepository.delete(user)
        
        // Xóa cache sau khi delete
        redisService.invalidateUserCache(userId)
    }

    /**
     * Get user statistics
     */
    fun getUserStats(userId: Long): AdminUserStatsDto {
        val user = getUserById(userId)
        
        val postsCount = postRepository.countByUserId(userId)
        val followersCount = followRepository.countByFollowedId(userId)
        val followingCount = followRepository.countByFollowerId(userId)
        val likesGivenCount = likeRepository.countByUserId(userId)
        val commentsGivenCount = commentRepository.countByUserId(userId)
        
        // Count likes received on user's posts
        val userPosts = postRepository.findByUserId(userId)
        val likesReceivedCount = userPosts.sumOf { it.likesCount.toLong() }
        val commentsReceivedCount = userPosts.sumOf { it.commentsCount.toLong() }
        
        return AdminUserStatsDto(
            userId = user.id,
            username = user.username,
            postsCount = postsCount,
            followersCount = followersCount,
            followingCount = followingCount,
            likesGivenCount = likesGivenCount,
            likesReceivedCount = likesReceivedCount,
            commentsGivenCount = commentsGivenCount,
            commentsReceivedCount = commentsReceivedCount
        )
    }
}
