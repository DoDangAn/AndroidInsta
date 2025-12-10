package com.androidinsta.Repository.User

import com.androidinsta.Model.Like
import com.androidinsta.Model.LikeId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LikeRepository : JpaRepository<Like, LikeId> {
    
    /**
     * Đếm số lượt like của một post
     */
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    fun countByPostId(@Param("postId") postId: Long): Long
    
    /**
     * Kiểm tra xem user đã like post chưa
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Like l WHERE l.user.id = :userId AND l.post.id = :postId")
    fun existsByUserIdAndPostId(
        @Param("userId") userId: Long,
        @Param("postId") postId: Long
    ): Boolean
    
    /**
     * Xóa like
     */
    @Query("DELETE FROM Like l WHERE l.user.id = :userId AND l.post.id = :postId")
    fun deleteByUserIdAndPostId(
        @Param("userId") userId: Long,
        @Param("postId") postId: Long
    )
    
    /**
     * Kiểm tra xem user đã like post chưa (tham số theo thứ tự postId, userId)
     */
    fun existsByPostIdAndUserId(postId: Long, userId: Long): Boolean
    
    /**
     * Xóa like (tham số theo thứ tự postId, userId)
     */
    @Query("DELETE FROM Like l WHERE l.post.id = :postId AND l.user.id = :userId")
    fun deleteByPostIdAndUserId(
        @Param("postId") postId: Long,
        @Param("userId") userId: Long
    )
    
    /**
     * Tìm like theo postId và userId
     */
    @Query("SELECT l FROM Like l WHERE l.post.id = :postId AND l.user.id = :userId")
    fun findByPostIdAndUserId(
        @Param("postId") postId: Long,
        @Param("userId") userId: Long
    ): Like?
    
    /**
     * Find likes created after a date
     */
    fun findByCreatedAtAfter(date: LocalDateTime): List<Like>
}
