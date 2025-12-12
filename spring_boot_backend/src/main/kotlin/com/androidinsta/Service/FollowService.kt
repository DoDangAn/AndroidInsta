package com.androidinsta.Service

import com.androidinsta.Model.Follow
import com.androidinsta.Repository.User.FollowRepository
import com.androidinsta.Repository.User.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val redisService: RedisService,
    private val notificationService: NotificationService
) {

    fun followUser(followerId: Long, followedId: Long): Boolean {
        if (followerId == followedId) {
            throw RuntimeException("Cannot follow yourself")
        }

        val follower = userRepository.findById(followerId)
            .orElseThrow { RuntimeException("Follower not found") }
        val followed = userRepository.findById(followedId)
            .orElseThrow { RuntimeException("User to follow not found") }

        // Check if already following
        if (followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            return false // Already following
        }

        val follow = Follow(
            follower = follower,
            followed = followed
        )

        followRepository.save(follow)

        // Send Kafka event
        kafkaProducerService.sendUserFollowedEvent(followerId, followedId)

        // Gửi notification
        notificationService.sendNotification(
            receiverId = followedId,
            senderId = followerId,
            type = com.androidinsta.Model.NotificationType.FOLLOW,
            entityId = null,
            message = "${follower.username} đã bắt đầu theo dõi bạn"
        )

        // Invalidate caches
        redisService.invalidateUserCache(followerId)
        redisService.invalidateUserCache(followedId)
        redisService.delete("following:count:$followerId")
        redisService.delete("followers:count:$followedId")

        return true
    }

    fun unfollowUser(followerId: Long, followedId: Long): Boolean {
        if (!followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            return false // Not following
        }

        followRepository.deleteByFollowerIdAndFollowedId(followerId, followedId)

        // Invalidate caches
        redisService.invalidateUserCache(followerId)
        redisService.invalidateUserCache(followedId)
        redisService.delete("following:count:$followerId")
        redisService.delete("followers:count:$followedId")
        
        // Send Kafka event for audit
        kafkaProducerService.sendUserUnfollowedEvent(followerId, followedId)

        return true
    }

    fun isFollowing(followerId: Long, followedId: Long): Boolean {
        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)
    }

    fun getFollowingCount(userId: Long): Long {
        val cacheKey = "following:count:$userId"
        val cached = redisService.get(cacheKey)
        if (cached != null) {
            return when (cached) {
                is Long -> cached
                is Int -> cached.toLong()
                is Number -> cached.toLong()
                else -> 0L
            }
        }
        
        val count = followRepository.countByFollowerId(userId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(30))
        return count
    }

    fun getFollowersCount(userId: Long): Long {
        val cacheKey = "followers:count:$userId"
        val cached = redisService.get(cacheKey)
        if (cached != null) {
            return when (cached) {
                is Long -> cached
                is Int -> cached.toLong()
                is Number -> cached.toLong()
                else -> 0L
            }
        }
        
        val count = followRepository.countByFollowedId(userId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(30))
        return count
    }

    fun getFollowers(userId: Long): List<com.androidinsta.Model.User> {
        return followRepository.findByFollowedId(userId).map { it.follower }
    }

    fun getFollowing(userId: Long): List<com.androidinsta.Model.User> {
        return followRepository.findByFollowerId(userId).map { it.followed }
    }

    /**
     * Get user stats với cache ở Service layer (DTO thuần, không cache ResponseEntity)
     */
    /**
     * Get user stats (follower count, following count, posts count)
     * DON'T cache complex DTOs - individual counters are already cached
     */
    fun getUserStats(userId: Long): com.androidinsta.dto.UserStatsDto {
        val followersCount = getFollowersCount(userId)
        val followingCount = getFollowingCount(userId)
        // TODO: implement posts count from PostRepository
        val postsCount = 0L

        return com.androidinsta.dto.UserStatsDto(
            followersCount = followersCount,
            followingCount = followingCount,
            postsCount = postsCount
        )
    }
}
