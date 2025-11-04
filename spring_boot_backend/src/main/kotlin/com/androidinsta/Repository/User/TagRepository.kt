package com.androidinsta.Repository.User

import com.androidinsta.Model.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<Tag, Long> {
    
    /**
     * Tìm tag theo tên chính xác
     */
    fun findByName(name: String): Tag?
    
    /**
     * Kiểm tra tag đã tồn tại
     */
    fun existsByName(name: String): Boolean
    
    /**
     * Tìm kiếm tags theo từ khóa
     */
    @Query("""
        SELECT t FROM Tag t 
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY t.name ASC
    """)
    fun searchTags(@Param("keyword") keyword: String, pageable: Pageable): Page<Tag>
    
    /**
     * Lấy top trending tags (tags có nhiều posts nhất)
     */
    @Query("""
        SELECT t FROM Tag t 
        LEFT JOIN t.posts p
        GROUP BY t.id
        ORDER BY COUNT(p.id) DESC
    """)
    fun findTrendingTags(pageable: Pageable): Page<Tag>
}
