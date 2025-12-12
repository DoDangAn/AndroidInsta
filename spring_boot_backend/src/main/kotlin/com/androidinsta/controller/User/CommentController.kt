package com.androidinsta.controller.User

import com.androidinsta.Service.CommentService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.CommentRequest
import com.androidinsta.dto.CommentResponse
import com.androidinsta.dto.MessageResponse
import com.androidinsta.dto.CountResponse
import jakarta.validation.Valid
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller cho comment operations
 * Handles adding, viewing, and deleting comments on posts
 */
@RestController
@RequestMapping("/api/posts/{postId}/comments")
class CommentController(
    private val commentService: CommentService
) {

    /**
     * Thêm comment hoặc reply comment
     * POST /api/posts/{postId}/comments
     */
    @CacheEvict(value = ["postComments", "commentReplies", "commentCount"], allEntries = true)
    @PostMapping
    fun addComment(
        @PathVariable postId: Long,
        @Valid @RequestBody request: CommentRequest
    ): ResponseEntity<CommentResponse> {
        val userId = SecurityUtil.getCurrentUserId()

        val comment = commentService.addComment(
            userId = userId,
            postId = postId,
            content = request.content.trim(),
            parentCommentId = request.parentCommentId
        )

        val repliesCount = commentService.getRepliesCount(comment.id).toInt()

        val response = CommentResponse(
            id = comment.id,
            postId = comment.post.id,
            userId = comment.user.id,
            username = comment.user.username,
            userAvatarUrl = comment.user.avatarUrl,
            content = comment.content,
            parentCommentId = comment.parentComment?.id,
            repliesCount = repliesCount,
            createdAt = comment.createdAt
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Lấy danh sách comments của post (chỉ comments gốc)
     * GET /api/posts/{postId}/comments
     */
    @GetMapping
    fun getPostComments(@PathVariable postId: Long): ResponseEntity<List<CommentResponse>> {
        val comments = commentService.getPostComments(postId)
        return ResponseEntity.ok(comments)
    }

    /**
     * Lấy replies của một comment
     * GET /api/posts/{postId}/comments/{commentId}/replies
     */
    @GetMapping("/{commentId}/replies")
    fun getCommentReplies(
        @PathVariable postId: Long,
        @PathVariable commentId: Long
    ): ResponseEntity<List<CommentResponse>> {
        val replies = commentService.getCommentReplies(commentId)
        return ResponseEntity.ok(replies)
    }

    /**
     * Xóa comment
     * DELETE /api/posts/{postId}/comments/{commentId}
     */
    @CacheEvict(value = ["postComments", "commentReplies", "commentCount"], allEntries = true)
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long
    ): ResponseEntity<MessageResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        
        commentService.deleteComment(userId, commentId)
        return ResponseEntity.ok(MessageResponse("Comment deleted successfully"))
    }

    /**
     * Lấy số lượng comments của post
     * GET /api/posts/{postId}/comments/count
     */
    @GetMapping("/count")
    fun getCommentCount(@PathVariable postId: Long): ResponseEntity<CountResponse> {
        val count = commentService.getCommentCount(postId)
        return ResponseEntity.ok(
            CountResponse(
                success = true,
                count = count,
                message = "Comment count retrieved successfully"
            )
        )
    }
}
