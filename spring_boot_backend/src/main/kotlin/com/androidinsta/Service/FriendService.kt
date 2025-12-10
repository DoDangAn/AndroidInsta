package com.androidinsta.Service

import com.androidinsta.Model.*
import com.androidinsta.Repository.User.FriendRequestRepository
import com.androidinsta.Repository.User.FriendshipRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class FriendService(
    private val friendRequestRepository: FriendRequestRepository,
    private val friendshipRepository: FriendshipRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val redisService: RedisService,
    private val kafkaProducerService: KafkaProducerService
) {

    // Gửi friend request
    @Transactional
    fun sendFriendRequest(senderId: Long, receiverId: Long): FriendRequestResponse {
        if (senderId == receiverId) {
            throw IllegalArgumentException("Cannot send friend request to yourself")
        }

        val sender = userRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("Sender not found") }
        val receiver = userRepository.findById(receiverId)
            .orElseThrow { IllegalArgumentException("Receiver not found") }

        // Kiểm tra đã là bạn bè chưa
        if (friendshipRepository.areFriends(senderId, receiverId)) {
            throw IllegalArgumentException("Already friends")
        }

        // Kiểm tra đã có pending request chưa
        val existingRequest = friendRequestRepository.findBetweenUsers(
            senderId, receiverId, FriendRequestStatus.PENDING
        )
        if (existingRequest != null) {
            throw IllegalArgumentException("Friend request already exists")
        }

        // Tạo friend request mới
        val friendRequest = FriendRequest(
            sender = sender,
            receiver = receiver,
            status = FriendRequestStatus.PENDING
        )
        val saved = friendRequestRepository.save(friendRequest)
        
        // Invalidate pending requests count cache
        redisService.delete("friend:pending:count:$receiverId")

        // Note: Database doesn't support FRIEND_REQUEST notification type
        // Notification feature can be added later if database schema is updated

        return toFriendRequestResponse(saved)
    }

    // Chấp nhận friend request
    @Transactional
    fun acceptFriendRequest(requestId: Long, userId: Long): FriendRequestResponse {
        val request = friendRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Friend request not found") }

        if (request.receiver.id != userId) {
            throw IllegalArgumentException("Not authorized to accept this request")
        }

        if (request.status != FriendRequestStatus.PENDING) {
            throw IllegalArgumentException("Friend request is not pending")
        }

        // Update request status
        request.status = FriendRequestStatus.ACCEPTED
        request.respondedAt = LocalDateTime.now()
        friendRequestRepository.save(request)

        // Tạo 2 friendship records (bidirectional)
        val friendship1 = Friendship(
            user = request.receiver,
            friend = request.sender
        )
        val friendship2 = Friendship(
            user = request.sender,
            friend = request.receiver
        )
        friendshipRepository.save(friendship1)
        friendshipRepository.save(friendship2)
        
        // Invalidate caches
        redisService.delete("friend:pending:count:$userId")
        redisService.delete("friends:count:${request.sender.id}")
        redisService.delete("friends:count:${request.receiver.id}")
        redisService.invalidateUserCache(request.sender.id)
        redisService.invalidateUserCache(request.receiver.id)
        
        // Send Kafka event
        kafkaProducerService.sendFriendAcceptEvent(
            userId = request.sender.id,
            friendId = request.receiver.id
        )

        // Note: Database doesn't support FRIEND_ACCEPT notification type
        // Notification feature can be added later if database schema is updated

        return toFriendRequestResponse(request)
    }

    // Từ chối friend request
    @Transactional
    fun rejectFriendRequest(requestId: Long, userId: Long): FriendRequestResponse {
        val request = friendRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Friend request not found") }

        if (request.receiver.id != userId) {
            throw IllegalArgumentException("Not authorized to reject this request")
        }

        if (request.status != FriendRequestStatus.PENDING) {
            throw IllegalArgumentException("Friend request is not pending")
        }

        request.status = FriendRequestStatus.REJECTED
        request.respondedAt = LocalDateTime.now()
        val saved = friendRequestRepository.save(request)
        
        // Invalidate pending requests count cache
        redisService.delete("friend:pending:count:$userId")

        return toFriendRequestResponse(saved)
    }

    // Hủy friend request đã gửi
    @Transactional
    fun cancelFriendRequest(requestId: Long, userId: Long): FriendRequestResponse {
        val request = friendRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Friend request not found") }

        if (request.sender.id != userId) {
            throw IllegalArgumentException("Not authorized to cancel this request")
        }

        if (request.status != FriendRequestStatus.PENDING) {
            throw IllegalArgumentException("Friend request is not pending")
        }

        request.status = FriendRequestStatus.CANCELLED
        request.respondedAt = LocalDateTime.now()
        val saved = friendRequestRepository.save(request)

        return toFriendRequestResponse(saved)
    }

    // Unfriend (xóa bạn)
    @Transactional
    fun unfriend(userId: Long, friendId: Long) {
        if (!friendshipRepository.areFriends(userId, friendId)) {
            throw IllegalArgumentException("Not friends")
        }

        // Xóa cả 2 chiều
        friendshipRepository.deleteFriendship(userId, friendId)
        friendshipRepository.deleteFriendship(friendId, userId)
        
        // Invalidate caches
        redisService.delete("friends:count:$userId")
        redisService.delete("friends:count:$friendId")
        redisService.invalidateUserCache(userId)
        redisService.invalidateUserCache(friendId)
    }

    // Lấy danh sách friend requests đã nhận
    fun getReceivedFriendRequests(userId: Long, pageable: Pageable): Page<FriendRequestResponse> {
        return friendRequestRepository.findPendingReceivedRequests(userId, pageable)
            .map { toFriendRequestResponse(it) }
    }

    // Lấy danh sách friend requests đã gửi
    fun getSentFriendRequests(userId: Long, pageable: Pageable): Page<FriendRequestResponse> {
        return friendRequestRepository.findPendingSentRequests(userId, pageable)
            .map { toFriendRequestResponse(it) }
    }

    // Đếm pending requests
    fun getPendingRequestsCount(userId: Long): Long {
        val cacheKey = "friend:pending:count:$userId"
        
        val cached = redisService.get(cacheKey, Long::class.java)
        if (cached != null) {
            return cached
        }
        
        val count = friendRequestRepository.countByReceiverIdAndStatus(userId, FriendRequestStatus.PENDING)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(5))
        
        return count
    }

    // Lấy danh sách bạn bè
    fun getFriends(userId: Long, pageable: Pageable): Page<FriendResponse> {
        val currentUserId = userId
        return friendshipRepository.findFriends(userId, pageable)
            .map { friendship ->
                val friend = friendship.friend
                val mutualCount = friendshipRepository.countMutualFriends(currentUserId, friend.id)
                
                FriendResponse(
                    id = friendship.id,
                    userId = friend.id,
                    username = friend.username,
                    fullName = friend.fullName,
                    avatarUrl = friend.avatarUrl,
                    bio = friend.bio,
                    mutualFriendsCount = mutualCount,
                    friendsSince = friendship.createdAt
                )
            }
    }

    // Lấy tất cả friend IDs (không phân trang) - dùng cho notifications
    fun getFriendIds(userId: Long): List<Long> {
        return friendshipRepository.findFriendIds(userId)
    }

    // Đếm số bạn bè
    fun getFriendsCount(userId: Long): Long {
        val cacheKey = "friends:count:$userId"
        
        val cached = redisService.get(cacheKey, Long::class.java)
        if (cached != null) {
            return cached
        }
        
        val count = friendshipRepository.countByUserId(userId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(30))
        
        return count
    }

    // Lấy mutual friends
    fun getMutualFriends(userId: Long, otherUserId: Long, pageable: Pageable): Page<FriendResponse> {
        return friendshipRepository.findMutualFriends(userId, otherUserId, pageable)
            .map { friendship ->
                val friend = friendship.friend
                val mutualCount = friendshipRepository.countMutualFriends(userId, friend.id)
                
                FriendResponse(
                    id = friendship.id,
                    userId = friend.id,
                    username = friend.username,
                    fullName = friend.fullName,
                    avatarUrl = friend.avatarUrl,
                    bio = friend.bio,
                    mutualFriendsCount = mutualCount,
                    friendsSince = friendship.createdAt
                )
            }
    }

    // Lấy friend suggestions
    fun getFriendSuggestions(userId: Long, pageable: Pageable): Page<FriendSuggestionResponse> {
        return friendshipRepository.findFriendSuggestions(userId, pageable)
            .map { user ->
                val mutualCount = friendshipRepository.countMutualFriends(userId, user.id)
                
                FriendSuggestionResponse(
                    userId = user.id,
                    username = user.username,
                    fullName = user.fullName,
                    avatarUrl = user.avatarUrl,
                    bio = user.bio,
                    mutualFriendsCount = mutualCount
                )
            }
    }

    // Kiểm tra friendship status
    fun getFriendshipStatus(userId: Long, otherUserId: Long): FriendshipStatusResponse {
        val isFriend = friendshipRepository.areFriends(userId, otherUserId)
        
        val pendingRequest = friendRequestRepository.findBetweenUsers(
            userId, otherUserId, FriendRequestStatus.PENDING
        )
        
        return FriendshipStatusResponse(
            isFriend = isFriend,
            hasPendingRequest = pendingRequest != null,
            pendingRequestSentByMe = pendingRequest?.sender?.id == userId,
            friendRequestId = pendingRequest?.id
        )
    }

    private fun toFriendRequestResponse(request: FriendRequest): FriendRequestResponse {
        return FriendRequestResponse(
            id = request.id,
            senderId = request.sender.id,
            senderUsername = request.sender.username,
            senderFullName = request.sender.fullName,
            senderAvatarUrl = request.sender.avatarUrl,
            receiverId = request.receiver.id,
            receiverUsername = request.receiver.username,
            receiverFullName = request.receiver.fullName,
            receiverAvatarUrl = request.receiver.avatarUrl,
            status = request.status,
            createdAt = request.createdAt,
            respondedAt = request.respondedAt
        )
    }
}
