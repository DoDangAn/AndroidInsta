package com.androidinsta.Service

import com.androidinsta.Model.Comment
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Repository.User.CommentRepository
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
        
        // Invalidate comment count cache
        redisService.delete("comment:count:$postId")
        parentCommentId?.let {
            redisService.delete("replies:count:$it")
        }

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
        
        // Invalidate caches
        redisService.delete("comment:count:${comment.post.id}")
        comment.parentComment?.let {
            redisService.delete("replies:count:${it.id}")
        }

        commentRepository.delete(comment)
    }

    fun getPostComments(postId: Long): List<Comment> {
        // Chỉ lấy comments gốc (không có parent)
        return commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId)
    }

    fun getCommentReplies(commentId: Long): List<Comment> {
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId)
    }

    fun getCommentCount(postId: Long): Long {
        val cacheKey = "comment:count:$postId"
        
        val cached = redisService.get(cacheKey, Long::class.java)
        if (cached != null) {
            return cached
        }
        
        val count = commentRepository.countByPostId(postId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(10))
        
        return count
    }

    fun getRepliesCount(commentId: Long): Long {
        val cacheKey = "replies:count:$commentId"
        
        val cached = redisService.get(cacheKey, Long::class.java)
        if (cached != null) {
            return cached
        }
        
        val count = commentRepository.countByParentCommentId(commentId)
        redisService.set(cacheKey, count, java.time.Duration.ofMinutes(10))
        
        return count
    }
}
