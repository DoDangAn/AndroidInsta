package com.androidinsta.Repository.User

import com.androidinsta.Model.Post
import com.androidinsta.Model.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    
    /**
     * Lấy feed: posts từ người follow, posts public, và ADVERTISE posts
     * Sắp xếp: ADVERTISE lên đầu, sau đó theo thời gian
     */
    @Query("""
        SELECT DISTINCT p FROM Post p 
        LEFT JOIN FETCH p.mediaFiles 
        LEFT JOIN FETCH p.user 
        WHERE (p.user.id IN :followedUserIds 
               OR p.visibility = 'PUBLIC' 
               OR p.visibility = 'ADVERTISE')
        ORDER BY 
            CASE WHEN p.visibility = 'ADVERTISE' THEN 0 ELSE 1 END,
            p.createdAt DESC
    """)
    fun findFeedPosts(
        @Param("followedUserIds") followedUserIds: List<Long>,
        pageable: Pageable
    ): Page<Post>
    
    /**
     * Lấy tất cả posts của một user
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<Post>
    
    /**
     * Lấy posts public của một user
     */
    fun findByUserIdAndVisibilityOrderByCreatedAtDesc(
        userId: Long, 
        visibility: Visibility, 
        pageable: Pageable
    ): Page<Post>
    
    /**
     * Lấy tất cả ADVERTISE posts (quảng cáo)
     */
    @Query("""
        SELECT p FROM Post p 
        LEFT JOIN FETCH p.mediaFiles 
        LEFT JOIN FETCH p.user 
        WHERE p.visibility = 'ADVERTISE'
        ORDER BY p.createdAt DESC
    """)
    fun findAdvertisePosts(pageable: Pageable): Page<Post>
    
    /**
     * Tìm kiếm posts theo từ khóa (caption hoặc username)
     */
    @Query("""
        SELECT DISTINCT p FROM Post p 
        LEFT JOIN p.user u 
        WHERE LOWER(p.caption) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.createdAt DESC
    """)
    fun searchPosts(@Param("keyword") keyword: String, pageable: Pageable): Page<Post>
}
