package com.androidinsta.repository.user

import com.androidinsta.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    // Nếu muốn thêm các truy vấn tùy chỉnh có thể khai báo ở đây
    fun findByUsername(username: String): User?
}
