package com.androidinsta.Repository.User

import com.androidinsta.Model.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    
    /**
     * Đếm số comment của một post
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    fun countByPostId(@Param("postId") postId: Long): Long
    
    /**
     * Lấy tất cả comments của một post (không bao gồm replies)
     */
    @Query("""
        SELECT c FROM Comment c 
        LEFT JOIN FETCH c.user 
        WHERE c.post.id = :postId AND c.parentComment IS NULL
        ORDER BY c.createdAt DESC
    """)
    fun findByPostIdOrderByCreatedAtDesc(
        @Param("postId") postId: Long,
        pageable: Pageable
    ): Page<Comment>
    
    /**
     * Lấy replies của một comment
     */
    @Query("""
        SELECT c FROM Comment c 
        LEFT JOIN FETCH c.user 
        WHERE c.parentComment.id = :parentCommentId
        ORDER BY c.createdAt ASC
    """)
    fun findRepliesByParentCommentId(
        @Param("parentCommentId") parentCommentId: Long
    ): List<Comment>
    
    /**
     * Lấy tất cả comments của một post (bao gồm cả replies) - không phân trang
     */
    fun findByPostIdOrderByCreatedAtDesc(postId: Long): List<Comment>
    
    /**
     * Lấy comments gốc của một post (không bao gồm replies) - không phân trang
     */
    fun findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId: Long): List<Comment>
    
    /**
     * Lấy replies của một comment - sắp xếp theo thời gian tăng dần
     */
    fun findByParentCommentIdOrderByCreatedAtAsc(parentCommentId: Long): List<Comment>
    
    /**
     * Đếm số replies của một comment
     */
    fun countByParentCommentId(parentCommentId: Long): Long
}
