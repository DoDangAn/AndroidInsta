package com.androidinsta.controller.User

import com.androidinsta.Service.AuthService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Authentication Controller
 * Handles user authentication, registration, and token management
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val authService: AuthService
) {

    /**
     * POST /api/auth/login - User login
     * @param loginRequest Login credentials
     * @return JWT tokens and user info
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        val jwtResponse = authService.login(loginRequest)
        return ResponseEntity.ok(
            AuthResponse(
                success = true,
                message = "Login successful",
                data = jwtResponse
            )
        )
    }
    
    /**
     * POST /api/auth/google - Google OAuth login
     * @param googleLoginRequest Google login credentials
     * @return JWT tokens and user info
     */
    @PostMapping("/google")
    fun googleLogin(@Valid @RequestBody googleLoginRequest: GoogleLoginRequest): ResponseEntity<AuthResponse> {
        val jwtResponse = authService.googleLogin(googleLoginRequest)
        return ResponseEntity.ok(
            AuthResponse(
                success = true,
                message = "Google login successful",
                data = jwtResponse
            )
        )
    }
    
    /**
     * POST /api/auth/register - User registration
     * @param registerRequest Registration data
     * @return JWT tokens and user info
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<AuthResponse> {
        val jwtResponse = authService.register(registerRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            AuthResponse(
                success = true,
                message = "Registration successful",
                data = jwtResponse
            )
        )
    }
    
    /**
     * POST /api/auth/signup - Alias for register (backward compatibility)
     */
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<AuthResponse> {
        return register(registerRequest)
    }

    /**
     * POST /api/auth/refresh-token - Refresh access token
     * @param tokenRefreshRequest Refresh token
     * @return New JWT tokens
     */
    @PostMapping("/refresh-token")
    fun refreshToken(@Valid @RequestBody tokenRefreshRequest: TokenRefreshRequest): ResponseEntity<TokenRefreshResponse> {
        val tokenResponse = authService.refreshToken(tokenRefreshRequest)
        return ResponseEntity.ok(tokenResponse)
    }

    /**
     * POST /api/auth/logout - User logout
     * Revokes all refresh tokens for the user
     */
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<LogoutResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        // Extract access token from header
        val authHeader = request.getHeader("Authorization")
        val accessToken = if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else null
        
        authService.logout(userId, accessToken)
        return ResponseEntity.ok(LogoutResponse(success = true, message = "Logout successful"))
    }

    /**
     * POST /api/auth/change-password - Change user password
     * @param changePasswordRequest Current and new password
     */
    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody changePasswordRequest: ChangePasswordRequest): ResponseEntity<PasswordChangeResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        authService.changePassword(userId, changePasswordRequest)
        return ResponseEntity.ok(
            PasswordChangeResponse(success = true, message = "Password changed successfully")
        )
    }

    /**
     * GET /api/auth/me - Get current authenticated user info
     */
    @GetMapping("/me")
    fun getCurrentUser(): ResponseEntity<CurrentUserResponse> {
        val userId = SecurityUtil.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        return ResponseEntity.ok(
            CurrentUserResponse(
                success = true,
                message = "User info retrieved successfully",
                data = CurrentUserData(
                    userId = userId,
                    username = SecurityUtil.getCurrentUsername() ?: "",
                    isAuthenticated = SecurityUtil.isAuthenticated()
                )
            )
        )
    }

    /**
     * GET /api/auth/validate-token - Validate JWT token
     */
    @GetMapping("/validate-token")
    fun validateToken(request: HttpServletRequest): ResponseEntity<TokenValidationResponse> {
        val authHeader = request.getHeader("Authorization")
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                TokenValidationResponse(
                    success = false,
                    message = "No token provided",
                    isAuthenticated = false
                )
            )
        }

        return ResponseEntity.ok(
            TokenValidationResponse(
                success = true,
                message = "Token is valid",
                isAuthenticated = SecurityUtil.isAuthenticated(),
                userId = SecurityUtil.getCurrentUserId()
            )
        )
    }
}
