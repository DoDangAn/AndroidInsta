package com.androidinsta.Repository.User

import com.androidinsta.Model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    // Tìm theo username
    fun findByUsername(username: String): Optional<User>
    
    // Tìm theo email
    fun findByEmail(email: String): Optional<User>
    
    // Tìm theo username hoặc email
    fun findByUsernameOrEmail(username: String, email: String): Optional<User>
    
    // Kiểm tra username đã tồn tại
    fun existsByUsername(username: String): Boolean
    
    // Kiểm tra email đã tồn tại
    fun existsByEmail(email: String): Boolean
    
    // Query custom - tìm kiếm theo username, fullName (không search email vì bảo mật)
    // Optimize: Chỉ search username và fullName, không expose email trong search
    @Query("""
        SELECT DISTINCT u FROM User u 
        WHERE u.isActive = true 
        AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) 
             OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY 
            CASE 
                WHEN LOWER(u.username) = LOWER(:keyword) THEN 0
                WHEN LOWER(u.username) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
                ELSE 2
            END,
            u.isVerified DESC
    """)
    fun searchUsers(keyword: String): List<User>
    
    // Search with pagination
    fun findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        username: String,
        email: String,
        pageable: Pageable
    ): Page<User>
    
    // Count by active status
    fun countByIsActive(isActive: Boolean): Long
    
    // Count by verified status
    fun countByIsVerified(isVerified: Boolean): Long
    
    // Count users created after a date
    fun countByCreatedAtAfter(date: LocalDateTime): Long
    
    // Find users created after a date
    fun findByCreatedAtAfter(date: LocalDateTime): List<User>
}