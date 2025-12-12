package com.androidinsta.controller.User

import com.androidinsta.dto.*
import com.androidinsta.Model.User
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.config.SecurityUtil
import com.androidinsta.Service.FollowService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * REST controller cho user operations
 * Handles user profiles, search, follow/unfollow functionality
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = ["*"])
class UserController(
    private val userRepository: UserRepository,
    private val securityUtil: SecurityUtil,
    private val passwordEncoder: PasswordEncoder,
    private val followService: FollowService
) {

    /**
     * Lấy profile của current user
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    fun getCurrentUserProfile(): ResponseEntity<UserProfileResponse> {
        val userId = securityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalStateException("User not found") }

        return ResponseEntity.ok(
            UserProfileResponse(
                success = true,
                message = "Profile retrieved successfully",
                data = user.toProfileData()
            )
        )
    }

    /**
     * Lấy user profile by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: Long): ResponseEntity<UserProfileResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalStateException("User not found") }

        return ResponseEntity.ok(
            UserProfileResponse(
                success = true,
                message = "User retrieved successfully",
                data = user.toProfileData()
            )
        )
    }

    /**
     * Tìm kiếm users
     * GET /api/users/search?keyword=john
     */
    @GetMapping("/search")
    fun searchUsers(@RequestParam keyword: String): ResponseEntity<UserListResponse> {
        val users = userRepository.searchUsers(keyword)
        val userProfiles = users.map { it.toProfileData() }

        return ResponseEntity.ok(
            UserListResponse(
                success = true,
                message = "Users found",
                data = userProfiles,
                count = userProfiles.size
            )
        )
    }

    /**
     * Follow một user
     * POST /api/users/{userId}/follow
     */
    @PostMapping("/{userId}/follow")
    fun followUser(@PathVariable userId: Long): ResponseEntity<FollowResponse> {
        val currentUserId = securityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val followed = followService.followUser(currentUserId, userId)
        return ResponseEntity.ok(
            FollowResponse(
                success = followed,
                message = if (followed) "User followed successfully" else "Already following this user",
                isFollowing = followed
            )
        )
    }

    /**
     * Unfollow một user
     * DELETE /api/users/{userId}/follow
     */
    @DeleteMapping("/{userId}/follow")
    fun unfollowUser(@PathVariable userId: Long): ResponseEntity<FollowResponse> {
        val currentUserId = securityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val unfollowed = followService.unfollowUser(currentUserId, userId)
        return ResponseEntity.ok(
            FollowResponse(
                success = unfollowed,
                message = if (unfollowed) "User unfollowed successfully" else "Not following this user",
                isFollowing = false
            )
        )
    }

    /**
     * Lấy danh sách followers
     * GET /api/users/{userId}/followers
     */
    @GetMapping("/{userId}/followers")
    fun getFollowers(@PathVariable userId: Long): ResponseEntity<UserListResponse> {
        val followers = followService.getFollowers(userId)
        val response = followers.map { it.toProfileData() }
        return ResponseEntity.ok(UserListResponse(success = true, data = response, count = response.size))
    }

    /**
     * Lấy danh sách following
     * GET /api/users/{userId}/following
     */
    @GetMapping("/{userId}/following")
    fun getFollowing(@PathVariable userId: Long): ResponseEntity<UserListResponse> {
        val following = followService.getFollowing(userId)
        val response = following.map { it.toProfileData() }
        return ResponseEntity.ok(UserListResponse(success = true, data = response, count = response.size))
    }

    /**
     * Cập nhật profile của current user
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    fun updateProfile(@Valid @RequestBody request: UpdateUserRequest): ResponseEntity<UpdateProfileResponse> {
        val userId = securityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalStateException("User not found") }

        val updatedUser = user.copy(
            fullName = request.fullName ?: user.fullName,
            bio = request.bio ?: user.bio,
            email = request.email ?: user.email,
            avatarUrl = request.avatarUrl ?: user.avatarUrl,
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(updatedUser)

        return ResponseEntity.ok(
            UpdateProfileResponse(
                success = true,
                message = "Profile updated successfully",
                data = savedUser.toProfileData()
            )
        )
    }

    /**
     * Kiểm tra follow status với một user
     * GET /api/users/{userId}/follow-status
     */
    @GetMapping("/{userId}/follow-status")
    fun getFollowStatus(@PathVariable userId: Long): ResponseEntity<FollowStatusResponse> {
        val currentUserId = securityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val isFollowing = followService.isFollowing(currentUserId, userId)
        val isFollower = followService.isFollowing(userId, currentUserId)
        
        return ResponseEntity.ok(
            FollowStatusResponse(
                success = true,
                isFollowing = isFollowing,
                isFollower = isFollower
            )
        )
    }

    /**
     * Lấy user statistics
     * GET /api/users/{userId}/stats
     */
    @GetMapping("/{userId}/stats")
    fun getUserStats(@PathVariable userId: Long): ResponseEntity<UserStatsDto> {
        val stats = followService.getUserStats(userId)
        return ResponseEntity.ok(stats)
    }
}