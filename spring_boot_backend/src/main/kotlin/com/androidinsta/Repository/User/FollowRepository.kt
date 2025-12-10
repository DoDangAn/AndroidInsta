package com.androidinsta.Repository.User

import com.androidinsta.Model.Follow
import com.androidinsta.Model.FollowId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface FollowRepository : JpaRepository<Follow, FollowId> {
    
    /**
     * Đếm số người theo dõi của một user
     */
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followed.id = :userId")
    fun countByFollowedId(@Param("userId") userId: Long): Long
    
    /**
     * Đếm số người mà user đang theo dõi
     */
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    fun countByFollowerId(@Param("userId") userId: Long): Long
    
    /**
     * Kiểm tra xem user A có đang theo dõi user B không
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Follow f WHERE f.follower.id = :followerId AND f.followed.id = :followedId")
    fun existsByFollowerIdAndFollowedId(
        @Param("followerId") followerId: Long,
        @Param("followedId") followedId: Long
    ): Boolean
    
    /**
     * Lấy danh sách ID của những người mà user đang theo dõi
     */
    @Query("SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId")
    fun findFollowedIdsByFollowerId(@Param("userId") userId: Long): List<Long>
    
    /**
     * Xóa follow relationship
     */
    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.followed.id = :followedId")
    fun deleteByFollowerIdAndFollowedId(
        @Param("followerId") followerId: Long,
        @Param("followedId") followedId: Long
    )

    /**
     * Lấy danh sách follow theo followerId
     */
    fun findByFollowerId(followerId: Long): List<Follow>

    /**
     * Lấy danh sách follow theo followedId
     */
    fun findByFollowedId(followedId: Long): List<Follow>
    
    /**
     * Find follows created after a date
     */
    fun findByCreatedAtAfter(date: LocalDateTime): List<Follow>
}
