package com.androidinsta.Service

import com.androidinsta.Model.Comment
import com.androidinsta.Repository.User.PostRepository
import com.androidinsta.Repository.User.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByPostIdOrderByCreatedAtDesc(postId: Long): List<Comment>
    fun findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId: Long): List<Comment>
    fun findByParentCommentIdOrderByCreatedAtAsc(parentCommentId: Long): List<Comment>
    fun countByPostId(postId: Long): Long
    fun countByParentCommentId(parentCommentId: Long): Int
}

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService,
    `private val notificationService: NotificationService,
    private val friendshipRepository: com.androidinsta.Repository.User.FriendshipRepository
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
                    type = com.androidinsta.Model.NotificationType.REPLY,
                    entityId = postId,
                    message = "${user.username} đã trả lời bình luận của bạn: \"$content\""
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
                    message = "${user.username} đã bình luận về bài viết của bạn: \"$content\""
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
        return commentRepository.countByPostId(postId)
    }

    fun getRepliesCount(commentId: Long): Int {
        return commentRepository.countByParentCommentId(commentId)
    }
}
