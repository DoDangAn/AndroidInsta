package com.androidinsta.controller.Admin

import com.androidinsta.Model.User
import com.androidinsta.Service.Admin.AdminUserService
import com.androidinsta.dto.AdminUserDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(private val adminUserService: AdminUserService) {

    /**
     * Get all users with pagination and search
     * GET /api/admin/users?page=0&size=20&keyword=john&sortBy=createdAt&direction=DESC
     */
    @GetMapping
    @org.springframework.cache.annotation.Cacheable(
        value = ["adminAllUsers"],
        key = "#keyword + '_page_' + #page + '_size_' + #size + '_sort_' + #sortBy + '_' + #direction"
    )
    fun getAllUsers(
        @RequestParam(required = false, defaultValue = "") keyword: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @RequestParam(required = false, defaultValue = "createdAt") sortBy: String,
        @RequestParam(required = false, defaultValue = "DESC") direction: String
    ): ResponseEntity<Map<String, Any>> {
        val sortDir = if (direction.equals("DESC", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy))
        val usersPage = adminUserService.getAllUsers(keyword, pageable)
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "users" to usersPage.content.map { AdminUserDto.fromUser(it) },
                "currentPage" to usersPage.number,
                "totalPages" to usersPage.totalPages,
                "totalItems" to usersPage.totalElements
            )
        )
    }

    /**
     * Get user by ID with details
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    @org.springframework.cache.annotation.Cacheable(
        value = ["adminUserById"],
        key = "#userId"
    )
    fun getUserById(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        val user = adminUserService.getUserById(userId)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "user" to AdminUserDto.fromUserWithDetails(user)
            )
        )
    }

    /**
     * Ban a user (set isActive = false)
     * PUT /api/admin/users/{userId}/ban
     */
    @PutMapping("/{userId}/ban")
    @org.springframework.cache.annotation.CacheEvict(
        value = ["adminAllUsers", "adminUserById", "adminUserDetailStats", "userProfile", "userStats"],
        allEntries = true
    )
    fun banUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.banUser(userId)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "User banned successfully"
            )
        )
    }

    /**
     * Unban a user (set isActive = true)
     * PUT /api/admin/users/{userId}/unban
     */
    @PutMapping("/{userId}/unban")
    @org.springframework.cache.annotation.CacheEvict(
        value = ["adminAllUsers", "adminUserById", "adminUserDetailStats", "userProfile", "userStats"],
        allEntries = true
    )
    fun unbanUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.unbanUser(userId)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "User unbanned successfully"
            )
        )
    }

    /**
     * Verify a user (set isVerified = true)
     * PUT /api/admin/users/{userId}/verify
     */
    @PutMapping("/{userId}/verify")
    @org.springframework.cache.annotation.CacheEvict(
        value = ["adminAllUsers", "adminUserById", "userProfile"],
        allEntries = true
    )
    fun verifyUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.verifyUser(userId)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "User verified successfully"
            )
        )
    }

    /**
     * Unverify a user (set isVerified = false)
     * PUT /api/admin/users/{userId}/unverify
     */
    @PutMapping("/{userId}/unverify")
    fun unverifyUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.unverifyUser(userId)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "User unverified successfully"
            )
        )
    }

    /**
     * Delete a user permanently
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    @org.springframework.cache.annotation.CacheEvict(
        value = ["adminAllUsers", "adminUserById", "adminUserDetailStats", "userProfile", "userStats", "userPosts", "feedPosts"],
        allEntries = true
    )
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.deleteUser(userId)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "User deleted successfully"
            )
        )
    }

    /**
     * Get user statistics
     * GET /api/admin/users/{userId}/stats
     */
    @GetMapping("/{userId}/stats")
    @org.springframework.cache.annotation.Cacheable(
        value = ["adminUserDetailStats"],
        key = "#userId"
    )
    fun getUserStats(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        val stats = adminUserService.getUserStats(userId)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "stats" to stats
            )
        )
    }
}
