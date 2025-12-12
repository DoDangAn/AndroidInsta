package com.androidinsta.controller.Admin

import com.androidinsta.Service.Admin.AdminUserService
import com.androidinsta.dto.*
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * REST controller cho admin user management
 * Handles user administration: view, ban, verify, delete users
 * Requires ADMIN role for all endpoints
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(private val adminUserService: AdminUserService) {

    /**
     * Lấy danh sách users với pagination và search
     * GET /api/admin/users?page=0&size=20&keyword=john&sortBy=createdAt&direction=DESC
     */
    @GetMapping
    fun getAllUsers(
        @RequestParam(defaultValue = "") keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") direction: String
    ): ResponseEntity<AdminUserListResponse> {
        val sortDir = if (direction.equals("DESC", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy))
        val usersPage = adminUserService.getAllUsers(keyword, pageable)
        
        return ResponseEntity.ok(
            AdminUserListResponse(
                success = true,
                users = usersPage.content.map { AdminUserData.fromUser(it) },
                currentPage = usersPage.number,
                totalPages = usersPage.totalPages,
                totalItems = usersPage.totalElements
            )
        )
    }

    /**
     * Lấy user by ID với details
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: Long): ResponseEntity<AdminActionResponse> {
        val user = adminUserService.getUserById(userId)
        return ResponseEntity.ok(
            AdminActionResponse(
                success = true,
                message = "User retrieved successfully",
                data = AdminUserData.fromUser(user)
            )
        )
    }

    /**
     * Ban user (set isActive = false)
     * PUT /api/admin/users/{userId}/ban
     */
    @PutMapping("/{userId}/ban")
    @CacheEvict(value = ["adminAllUsers", "adminUserById", "adminUserDetailStats", "userProfile", "userStats"], allEntries = true)
    fun banUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.banUser(userId)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "User banned successfully"))
    }

    /**
     * Unban user (set isActive = true)
     * PUT /api/admin/users/{userId}/unban
     */
    @PutMapping("/{userId}/unban")
    @CacheEvict(value = ["adminAllUsers", "adminUserById", "adminUserDetailStats", "userProfile", "userStats"], allEntries = true)
    fun unbanUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.unbanUser(userId)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "User unbanned successfully"))
    }

    /**
     * Verify user (set isVerified = true)
     * PUT /api/admin/users/{userId}/verify
     */
    @PutMapping("/{userId}/verify")
    @CacheEvict(value = ["adminAllUsers", "adminUserById", "userProfile"], allEntries = true)
    fun verifyUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.verifyUser(userId)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "User verified successfully"))
    }

    /**
     * Unverify user (set isVerified = false)
     * PUT /api/admin/users/{userId}/unverify
     */
    @PutMapping("/{userId}/unverify")
    fun unverifyUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.unverifyUser(userId)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "User unverified successfully"))
    }

    /**
     * Xóa user permanently
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    @CacheEvict(value = ["adminAllUsers", "adminUserById", "adminUserDetailStats", "userProfile", "userStats", "userPosts", "feedPosts"], allEntries = true)
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        adminUserService.deleteUser(userId)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "User deleted successfully"))
    }

    /**
     * Lấy user statistics
     * GET /api/admin/users/{userId}/stats
     */
    @GetMapping("/{userId}/stats")
    fun getUserStats(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        val stats = adminUserService.getUserStats(userId)
        return ResponseEntity.ok(mapOf("success" to true, "stats" to stats))
    }
}
