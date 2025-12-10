package com.androidinsta.controller.User

import com.androidinsta.Service.CommentService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.CommentRequest
import com.androidinsta.dto.CommentResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts/{postId}/comments")
class CommentController(
    private val commentService: CommentService
) {

    /**
     * Thêm comment hoặc reply comment
     * POST /api/posts/{postId}/comments
     */
    @org.springframework.cache.annotation.CacheEvict(value = ["postComments", "commentReplies", "commentCount"], allEntries = true)
    @PostMapping
    fun addComment(
        @PathVariable postId: Long,
        @RequestBody request: CommentRequest
    ): ResponseEntity<CommentResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        
        if (request.content.isBlank()) {
            return ResponseEntity.badRequest().build()
        }

        val comment = commentService.addComment(
            userId = userId,
            postId = postId,
            content = request.content.trim(),
            parentCommentId = request.parentCommentId
        )

        val repliesCount = commentService.getRepliesCount(comment.id)

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
    @org.springframework.cache.annotation.Cacheable(value = ["postComments"], key = "#postId")
    @GetMapping
    fun getPostComments(@PathVariable postId: Long): ResponseEntity<List<CommentResponse>> {
        val comments = commentService.getPostComments(postId)

        val responses = comments.map { comment ->
            val repliesCount = commentService.getRepliesCount(comment.id)
            
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

        return ResponseEntity.ok(responses)
    }

    /**
     * Lấy replies của một comment
     * GET /api/posts/{postId}/comments/{commentId}/replies
     */
    @org.springframework.cache.annotation.Cacheable(value = ["commentReplies"], key = "#commentId")
    @GetMapping("/{commentId}/replies")
    fun getCommentReplies(
        @PathVariable postId: Long,
        @PathVariable commentId: Long
    ): ResponseEntity<List<CommentResponse>> {
        val replies = commentService.getCommentReplies(commentId)

        val responses = replies.map { reply ->
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

        return ResponseEntity.ok(responses)
    }

    /**
     * Xóa comment
     * DELETE /api/posts/{postId}/comments/{commentId}
     */
    @org.springframework.cache.annotation.CacheEvict(value = ["postComments", "commentReplies", "commentCount"], allEntries = true)
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long
    ): ResponseEntity<Map<String, String>> {
        val userId = SecurityUtil.getCurrentUserId()
        
        try {
            commentService.deleteComment(userId, commentId)
            return ResponseEntity.ok(mapOf("message" to "Comment deleted successfully"))
        } catch (e: RuntimeException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to e.message!!))
        }
    }

    /**
     * Lấy số lượng comments của post
     * GET /api/posts/{postId}/comments/count
     */
    @org.springframework.cache.annotation.Cacheable(value = ["commentCount"], key = "#postId")
    @GetMapping("/count")
    fun getCommentCount(@PathVariable postId: Long): ResponseEntity<Map<String, Long>> {
        val count = commentService.getCommentCount(postId)
        return ResponseEntity.ok(mapOf("count" to count))
    }
}
