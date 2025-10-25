package com.androidinsta.controller.User

import com.androidinsta.Service.AuthService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        return try {
            val jwtResponse = authService.login(loginRequest)
            ResponseEntity.ok(
                AuthResponse(
                    success = true,
                    message = "Login successful",
                    data = jwtResponse
                )
            )
        } catch (e: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                AuthResponse(
                    success = false,
                    message = e.message ?: "Invalid credentials"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                AuthResponse(
                    success = false,
                    message = e.message ?: "Login failed"
                )
            )
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<AuthResponse> {
        return try {
            val jwtResponse = authService.register(registerRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse(
                    success = true,
                    message = "Registration successful",
                    data = jwtResponse
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                AuthResponse(
                    success = false,
                    message = e.message ?: "Registration failed"
                )
            )
        }
    }

    @PostMapping("/refresh-token")
    fun refreshToken(@Valid @RequestBody tokenRefreshRequest: TokenRefreshRequest): ResponseEntity<*> {
        return try {
            val tokenResponse = authService.refreshToken(tokenRefreshRequest)
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Token refreshed successfully",
                    "data" to tokenResponse
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Token refresh failed")
                )
            )
        }
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<*> {
        return try {
            val userId = SecurityUtil.getCurrentUserId()
                ?: throw RuntimeException("User not authenticated")
            
            // Extract access token from header
            val authHeader = request.getHeader("Authorization")
            val accessToken = if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader.substring(7)
            } else null
            
            authService.logout(userId, accessToken)
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Logout successful"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Logout failed")
                )
            )
        }
    }

    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody changePasswordRequest: ChangePasswordRequest): ResponseEntity<*> {
        return try {
            val userId = SecurityUtil.getCurrentUserId()
                ?: throw RuntimeException("User not authenticated")
            
            authService.changePassword(userId, changePasswordRequest)
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Password changed successfully"
                )
            )
        } catch (e: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Current password is incorrect")
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Password change failed")
                )
            )
        }
    }

    @GetMapping("/me")
    fun getCurrentUser(): ResponseEntity<*> {
        return try {
            val userId = SecurityUtil.getCurrentUserId()
                ?: throw RuntimeException("User not authenticated")
            
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "User info retrieved successfully",
                    "data" to mapOf(
                        "userId" to userId,
                        "username" to SecurityUtil.getCurrentUsername(),
                        "isAuthenticated" to SecurityUtil.isAuthenticated()
                    )
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "User not authenticated")
                )
            )
        }
    }

    @GetMapping("/validate-token")
    fun validateToken(request: HttpServletRequest): ResponseEntity<*> {
        val authHeader = request.getHeader("Authorization")
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf(
                    "success" to false,
                    "message" to "No token provided"
                )
            )
        }

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Token is valid",
                "data" to mapOf(
                    "isAuthenticated" to SecurityUtil.isAuthenticated(),
                    "userId" to SecurityUtil.getCurrentUserId()
                )
            )
        )
    }
}
