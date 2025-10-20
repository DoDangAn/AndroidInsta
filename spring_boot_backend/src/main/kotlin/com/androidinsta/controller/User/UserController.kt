package com.androidinsta.controller

import com.androidinsta.dto.*
import com.androidinsta.Model.User
import com.androidinsta.Repository.UserRepository
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
    private val passwordEncoder: PasswordEncoder
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
}

// Extension function để convert User sang UserResponse
private fun User.toResponse() = UserResponse(
    id = this.id,
    username = this.username,
    email = this.email,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)