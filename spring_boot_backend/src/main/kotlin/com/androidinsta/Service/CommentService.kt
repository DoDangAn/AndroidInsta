package com.androidinsta.Service

import com.androidinsta.Model.Comment
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Repository.User.CommentRepository
import com.androidinsta.dto.CommentResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val notificationService: NotificationService,
    private val friendshipRepository: com.androidinsta.Repository.User.FriendshipRepository,
    private val redisService: RedisService
) {

    fun addComment(userId: Long, postId: Long, content: String, parentCommentId: Long? = null): Comment {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }
        val post = postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found") }

        // Nếu là reply, kiểm tra parent comment tồn tại
        val parentComment = parentCommentId?.let { 
            commentRepository.findById(it)
                .orElseThrow { RuntimeException("Parent comment not found") }
        }

        val comment = Comment(
            user = user,
            post = post,
            content = content,
            parentComment = parentComment
        )

        val savedComment = commentRepository.save(comment)
        
        // Invalidate simple counter caches only (NOT DTOs)
        redisService.delete("post:${postId}:commentCount")
        redisService.delete("comment:${parentCommentId}:repliesCount")

        // Send Kafka event
        kafkaProducerService.sendPostCommentedEvent(
            postId = postId,
            commentId = savedComment.id,
            userId = userId,
            content = content
        )

        // Gửi notification (chỉ khi là bạn bè)
        if (parentComment != null) {
            // Reply comment - gửi cho người được reply
            if (parentComment.user.id != userId && friendshipRepository.areFriends(userId, parentComment.user.id)) {
                notificationService.sendNotification(
                    receiverId = parentComment.user.id,
                    senderId = userId,
                    type = com.androidinsta.Model.NotificationType.COMMENT,
                    entityId = postId,
                    message = null
                )
            }
        } else {
            // Comment gốc - gửi cho chủ bài viết
            if (post.user.id != userId && friendshipRepository.areFriends(userId, post.user.id)) {
                notificationService.sendNotification(
                    receiverId = post.user.id,
                    senderId = userId,
                    type = com.androidinsta.Model.NotificationType.COMMENT,
                    entityId = postId,
                    message = null
                )
            }
        }

        return savedComment
    }

    fun deleteComment(userId: Long, commentId: Long) {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { RuntimeException("Comment not found") }

        if (comment.user.id != userId) {
            throw RuntimeException("You can only delete your own comments")
        }
        
        // Invalidate simple counter caches only (NOT DTOs)
        redisService.delete("post:${comment.post.id}:commentCount")
        comment.parentComment?.let {
            redisService.delete("comment:${it.id}:repliesCount")
        }

        commentRepository.delete(comment)
    }

    fun getPostComments(postId: Long): List<CommentResponse> {
        // DON'T cache complex DTOs - query is fast with database indexes
        // Only cache simple comment count separately
        
        // Chỉ lấy comments gốc (không có parent)
        val comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId)
        
        // Map to DTO inside transaction to avoid lazy loading issues
        val result = comments.map { comment ->
            val repliesCount = getRepliesCount(comment.id).toInt()
            
            CommentResponse(
                id = comment.id,
                postId = comment.post.id,
                userId = comment.user.id,
                username = comment.user.username,
                userAvatarUrl = comment.user.avatarUrl,
                content = comment.content,
                parentCommentId = null,
                repliesCount = repliesCount,
                createdAt = comment.createdAt
            )
        }
        
        return result
    }

    fun getCommentReplies(commentId: Long): List<CommentResponse> {
        // DON'T cache complex DTOs - query is fast with database indexes
        
        val replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId)
        
        // Map to DTO inside transaction to avoid lazy loading issues
        val result = replies.map { reply ->
            CommentResponse(
                id = reply.id,
                postId = reply.post.id,
                userId = reply.user.id,
                username = reply.user.username,
                userAvatarUrl = reply.user.avatarUrl,
                content = reply.content,
                parentCommentId = reply.parentComment?.id,
                repliesCount = 0, // Replies thường không có nested replies
                createdAt = reply.createdAt
            )
        }
        
        return result
    }

    fun getCommentCount(postId: Long): Long {
        // ✅ Cache simple counter (Long) - this is good!
        val cacheKey = "post:${postId}:commentCount"
        
        val cached = redisService.get(cacheKey)
        if (cached != null) {
            // Handle both Integer and Long from Redis
            return when (cached) {
                is Long -> cached
                is Int -> cached.toLong()
                is Number -> cached.toLong()
                else -> 0L
            }
        }
        
        val count = commentRepository.countByPostId(postId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(10))
        
        return count
    }

    fun getRepliesCount(commentId: Long): Long {
        val cacheKey = "replies:count:$commentId"
        
        val cached = redisService.get(cacheKey)
        if (cached != null) {
            // Handle both Integer and Long from Redis
            return when (cached) {
                is Long -> cached
                is Int -> cached.toLong()
                is Number -> cached.toLong()
                else -> 0L
            }
        }
        
        val count = commentRepository.countByParentCommentId(commentId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(10))
        
        return count
    }
}
