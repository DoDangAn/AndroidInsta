package com.androidinsta.controller.Admin

import com.androidinsta.Service.CommentService
import com.androidinsta.dto.CommentResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/comments")
@PreAuthorize("hasRole('ADMIN')")
class AdminCommentController(
    private val commentService: CommentService
) {

    /**
     * Get all comments (for moderation)
     * GET /api/admin/comments?page=0&size=50
     */
    @GetMapping
    fun getAllComments(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val comments = commentService.getAllCommentsForAdmin(page, size)
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "comments" to comments.content.map { comment ->
                    mapOf(
                        "id" to comment.id,
                        "content" to comment.content,
                        "postId" to comment.post.id,
                        "userId" to comment.user.id,
                        "username" to comment.user.username,
                        "userAvatarUrl" to comment.user.avatarUrl,
                        "parentCommentId" to comment.parentComment?.id,
                        "createdAt" to comment.createdAt,
                        "postCaption" to (comment.post.caption ?: "No caption")
                    )
                },
                "currentPage" to comments.number,
                "totalPages" to comments.totalPages,
                "totalItems" to comments.totalElements
            )
        )
    }

    /**
     * Delete any comment (admin power)
     * DELETE /api/admin/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    fun deleteComment(@PathVariable commentId: Long): ResponseEntity<Map<String, Any>> {
        commentService.deleteCommentByAdmin(commentId)
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Comment deleted successfully by admin"
            )
        )
    }

    /**
     * Get comments by post ID (for moderation)
     * GET /api/admin/posts/{postId}/comments
     */
    @GetMapping("/post/{postId}")
    fun getCommentsByPost(@PathVariable postId: Long): ResponseEntity<Map<String, Any>> {
        val comments = commentService.getCommentsByPostForAdmin(postId)
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "comments" to comments.map { comment ->
                    mapOf(
                        "id" to comment.id,
                        "content" to comment.content,
                        "userId" to comment.user.id,
                        "username" to comment.user.username,
                        "userAvatarUrl" to comment.user.avatarUrl,
                        "parentCommentId" to comment.parentComment?.id,
                        "createdAt" to comment.createdAt
                    )
                }
            )
        )
    }

    /**
     * Get comments by user ID (for moderation)
     * GET /api/admin/users/{userId}/comments
     */
    @GetMapping("/user/{userId}")
    fun getCommentsByUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        val comments = commentService.getCommentsByUserForAdmin(userId)
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "comments" to comments.map { comment ->
                    mapOf(
                        "id" to comment.id,
                        "content" to comment.content,
                        "postId" to comment.post.id,
                        "postCaption" to (comment.post.caption ?: "No caption"),
                        "parentCommentId" to comment.parentComment?.id,
                        "createdAt" to comment.createdAt
                    )
                }
            )
        )
    }

    /**
     * Bulk delete comments
     * DELETE /api/admin/comments/bulk
     */
    @DeleteMapping("/bulk")
    fun bulkDeleteComments(@RequestBody commentIds: List<Long>): ResponseEntity<Map<String, Any>> {
        val deletedCount = commentService.bulkDeleteComments(commentIds)
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Deleted $deletedCount comments",
                "deletedCount" to deletedCount
            )
        )
    }
}
