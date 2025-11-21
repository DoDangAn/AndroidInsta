package com.androidinsta.Repository.User

import com.androidinsta.Model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
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
    
    // Query custom - tìm kiếm theo username, email, hoặc fullName
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.fullName LIKE %:keyword%")
    fun searchUsers(keyword: String): List<User>
}