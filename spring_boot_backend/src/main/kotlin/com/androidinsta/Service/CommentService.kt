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
    fun countByPostId(postId: Long): Long
}

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService
) {

    fun addComment(userId: Long, postId: Long, content: String): Comment {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }
        val post = postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found") }

        val comment = Comment(
            user = user,
            post = post,
            content = content
        )

        val savedComment = commentRepository.save(comment)

        // Send Kafka event
        kafkaProducerService.sendPostCommentedEvent(
            postId = postId,
            commentId = savedComment.id,
            userId = userId,
            content = content
        )

        // Send notification to post owner if different user
        if (post.user.id != userId) {
            kafkaProducerService.sendNotificationEvent(
                userId = post.user.id,
                title = "New Comment",
                message = "${user.username} commented on your post",
                type = "COMMENT"
            )
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
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
    }

    fun getCommentCount(postId: Long): Long {
        return commentRepository.countByPostId(postId)
    }
}
