package com.androidinsta.Service

import com.androidinsta.Model.Follow
import com.androidinsta.Repository.User.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Repository
interface FollowRepository : JpaRepository<Follow, Long> {
    fun findByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Follow?
    fun existsByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Boolean
    fun deleteByFollowerIdAndFollowedId(followerId: Long, followedId: Long)
    fun countByFollowerId(followerId: Long): Long
    fun countByFollowedId(followedId: Long): Long
}

@Service
@Transactional
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val redisService: RedisService
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

        // Send notification
        kafkaProducerService.sendNotificationEvent(
            userId = followedId,
            title = "New Follower",
            message = "${follower.username} started following you",
            type = "FOLLOW"
        )

        // Invalidate caches
        redisService.invalidateUserCache(followerId)
        redisService.invalidateUserCache(followedId)

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

        return true
    }

    fun isFollowing(followerId: Long, followedId: Long): Boolean {
        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)
    }

    fun getFollowingCount(userId: Long): Long {
        return followRepository.countByFollowerId(userId)
    }

    fun getFollowersCount(userId: Long): Long {
        return followRepository.countByFollowedId(userId)
    }
}
