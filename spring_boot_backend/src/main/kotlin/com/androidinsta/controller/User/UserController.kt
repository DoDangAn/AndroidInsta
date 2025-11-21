package com.androidinsta.controller

import com.androidinsta.dto.*
import com.androidinsta.Model.User
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.config.SecurityUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = ["*"])
class UserController(
    private val userRepository: UserRepository,
    private val securityUtil: SecurityUtil,
    private val passwordEncoder: PasswordEncoder,
    private val followService: com.androidinsta.Service.FollowService
) {

    @GetMapping("/profile")
    fun getCurrentUserProfile(): ResponseEntity<*> {
        return try {
            val userId = securityUtil.getCurrentUserId()
                ?: throw RuntimeException("User not authenticated")

            val user = userRepository.findById(userId)
                .orElseThrow { RuntimeException("User not found") }

            val userResponse = UserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                bio = user.bio,
                avatarUrl = user.avatarUrl,
                isVerified = user.isVerified,
                isActive = user.isActive,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Profile retrieved successfully",
                    "data" to userResponse
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Failed to get profile")
                )
            )
        }
    }

    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: Long): ResponseEntity<*> {
        return try {
            val user = userRepository.findById(userId)
                .orElseThrow { RuntimeException("User not found") }

            val userResponse = UserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                bio = user.bio,
                avatarUrl = user.avatarUrl,
                isVerified = user.isVerified,
                isActive = user.isActive,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "User retrieved successfully",
                    "data" to userResponse
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "User not found")
                )
            )
        }
    }

    @GetMapping("/search")
    fun searchUsers(@RequestParam keyword: String): ResponseEntity<*> {
        return try {
            val users = userRepository.searchUsers(keyword)

            val userResponses = users.map { user ->
                UserResponse(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
            }

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Users found",
                    "data" to userResponses,
                    "count" to userResponses.size
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Search failed")
                )
            )
        }
    }

    /**
     * POST /api/users/{userId}/follow - Follow a user
     */
    @PostMapping("/{userId}/follow")
    fun followUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
// duplicate block removed
        val currentUserId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf(
                "success" to false,
                "message" to "Unauthorized"
            ))

        return try {
            val followed = followService.followUser(currentUserId, userId)
            ResponseEntity.ok(mapOf(
                "success" to followed,
                "message" to if (followed) "User followed successfully" else "Already following this user"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to (e.message ?: "Follow failed")
            ))
        }
    }

    /**
     * DELETE /api/users/{userId}/follow - Unfollow a user
     */
    @DeleteMapping("/{userId}/follow")
    fun unfollowUser(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        val currentUserId = SecurityUtil.getCurrentUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf(
                "success" to false,
                "message" to "Unauthorized"
            ))

        return try {
            val unfollowed = followService.unfollowUser(currentUserId, userId)
            ResponseEntity.ok(mapOf(
                "success" to unfollowed,
                "message" to if (unfollowed) "User unfollowed successfully" else "Not following this user"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to (e.message ?: "Unfollow failed")
            ))
        }
    }
    @GetMapping("/{userId}/followers")
    fun getFollowers(@PathVariable userId: Long): ResponseEntity<*> {
        return try {
            val followers = followService.getFollowers(userId)
            val response = followers.map { user ->
                UserResponse(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    fullName = user.fullName,
                    bio = user.bio,
                    avatarUrl = user.avatarUrl,
                    isVerified = user.isVerified,
                    isActive = user.isActive,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
            }
            ResponseEntity.ok(mapOf("success" to true, "data" to response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("success" to false, "message" to e.message))
        }
    }

    /**
     * GET /api/users/{userId}/following - Get following list
     */
    @GetMapping("/{userId}/following")
    fun getFollowing(@PathVariable userId: Long): ResponseEntity<*> {
        return try {
            val following = followService.getFollowing(userId)
            val response = following.map { user ->
                UserResponse(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    fullName = user.fullName,
                    bio = user.bio,
                    avatarUrl = user.avatarUrl,
                    isVerified = user.isVerified,
                    isActive = user.isActive,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
            }
            ResponseEntity.ok(mapOf("success" to true, "data" to response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("success" to false, "message" to e.message))
        }
    }

    /**
     * PUT /api/users/profile - Update current user's profile
     */
    @PutMapping("/profile")
    fun updateProfile(@RequestBody request: UpdateUserRequest): ResponseEntity<*> {
        return try {
            val userId = securityUtil.getCurrentUserId()
                ?: throw RuntimeException("User not authenticated")

            val user = userRepository.findById(userId)
                .orElseThrow { RuntimeException("User not found") }

            // Create updated user with new values
            val updatedUser = user.copy(
                fullName = request.fullName ?: user.fullName,
                bio = request.bio ?: user.bio,
                email = request.email ?: user.email,
                avatarUrl = request.avatarUrl ?: user.avatarUrl,
                updatedAt = LocalDateTime.now()
            )

            // Save updated user
            val savedUser = userRepository.save(updatedUser)

            val userResponse = UserResponse(
                id = savedUser.id,
                username = savedUser.username,
                email = savedUser.email,
                fullName = savedUser.fullName,
                bio = savedUser.bio,
                avatarUrl = savedUser.avatarUrl,
                isVerified = savedUser.isVerified,
                isActive = savedUser.isActive,
                createdAt = savedUser.createdAt,
                updatedAt = savedUser.updatedAt
            )

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Profile updated successfully",
                    "data" to userResponse
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Failed to update profile")
                )
            )
        }
    }
}