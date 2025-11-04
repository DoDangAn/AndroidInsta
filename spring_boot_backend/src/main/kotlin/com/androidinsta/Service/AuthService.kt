package com.androidinsta.Service

import com.androidinsta.Model.User
import com.androidinsta.Model.RefreshToken
import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.Repository.User.RoleRepository
import com.androidinsta.Repository.User.RefreshTokenRepository
import com.androidinsta.config.JwtUtil
import com.androidinsta.config.JwtProperties
import com.androidinsta.dto.LoginRequest
import com.androidinsta.dto.RegisterRequest
import com.androidinsta.dto.JwtResponse
import com.androidinsta.dto.TokenRefreshRequest
import com.androidinsta.dto.TokenRefreshResponse
import com.androidinsta.dto.ChangePasswordRequest
import com.androidinsta.dto.UserInfo
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val jwtProperties: JwtProperties,
    private val redisService: RedisService,
    private val kafkaProducerService: KafkaProducerService
) {

    fun login(loginRequest: LoginRequest): JwtResponse {
        // Tìm user theo username hoặc email
        val user = userRepository.findByUsernameOrEmail(
            loginRequest.usernameOrEmail, 
            loginRequest.usernameOrEmail
        ).orElseThrow { 
            BadCredentialsException("Invalid username/email or password") 
        }

        // Kiểm tra password
        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            throw BadCredentialsException("Invalid username/email or password")
        }

        // Kiểm tra user có active không
        if (!user.isActive) {
            throw RuntimeException("Account is deactivated")
        }

        // Tạo tokens
        return generateTokens(user)
    }

    fun register(registerRequest: RegisterRequest): JwtResponse {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(registerRequest.username)) {
            throw RuntimeException("Username already exists")
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(registerRequest.email)) {
            throw RuntimeException("Email already exists")
        }

        // Lấy role USER mặc định
        val userRole = roleRepository.findByName("USER")
            .orElseThrow { RuntimeException("Role USER not found") }

        // Tạo user mới
        val newUser = User(
            username = registerRequest.username,
            email = registerRequest.email,
            password = passwordEncoder.encode(registerRequest.password),
            fullName = registerRequest.fullName,
            role = userRole,
            createdAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(newUser)
        
        // Send user registered event to Kafka
        kafkaProducerService.sendUserRegisteredEvent(
            userId = savedUser.id,
            username = savedUser.username,
            email = savedUser.email
        )
        
        return generateTokens(savedUser)
    }

    fun refreshToken(tokenRefreshRequest: TokenRefreshRequest): TokenRefreshResponse {
        val refreshToken = refreshTokenRepository.findByToken(tokenRefreshRequest.refreshToken)
            .orElseThrow { RuntimeException("Refresh token not found") }

        // Kiểm tra token có bị revoke không
        if (refreshToken.revoked) {
            throw RuntimeException("Refresh token has been revoked")
        }

        // Check if refresh token is blacklisted in Redis
        if (redisService.isTokenBlacklisted(refreshToken.token)) {
            throw RuntimeException("Refresh token has been revoked")
        }

        // Kiểm tra token có hết hạn không
        if (refreshToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw RuntimeException("Refresh token has expired")
        }

        // Validate JWT refresh token
        if (!jwtUtil.validateToken(refreshToken.token) || 
            jwtUtil.getTokenTypeFromToken(refreshToken.token) != "refresh") {
            throw RuntimeException("Invalid refresh token")
        }

        val user = refreshToken.user
        val roles = listOf(user.role?.name ?: "USER")

        // Tạo access token mới
        val newAccessToken = jwtUtil.generateAccessToken(
            userId = user.id,
            username = user.username,
            roles = roles
        )

        return TokenRefreshResponse(
            accessToken = newAccessToken,
            expiresIn = jwtProperties.accessToken.expiration
        )
    }

    fun logout(userId: Long, accessToken: String? = null) {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }
        
        // Blacklist current access token if provided
        if (accessToken != null) {
            try {
                val expirationDate = jwtUtil.getExpirationDateFromToken(accessToken)
                val remainingMillis = expirationDate.time - System.currentTimeMillis()
                if (remainingMillis > 0) {
                    redisService.blacklistToken(accessToken, remainingMillis)
                }
            } catch (e: Exception) {
                // Token might be invalid, ignore
            }
        }
        
        // Revoke tất cả refresh tokens của user
        refreshTokenRepository.revokeAllUserTokens(user)
        
        // Invalidate user cache
        redisService.invalidateUserCache(userId)
    }

    fun changePassword(userId: Long, changePasswordRequest: ChangePasswordRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        // Kiểm tra current password
        if (!passwordEncoder.matches(changePasswordRequest.currentPassword, user.password)) {
            throw BadCredentialsException("Current password is incorrect")
        }

        // Cập nhật password mới
        val updatedUser = user.copy(
            password = passwordEncoder.encode(changePasswordRequest.newPassword),
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)

        // Revoke tất cả refresh tokens để force login lại
        refreshTokenRepository.revokeAllUserTokens(user)
    }

    private fun generateTokens(user: User): JwtResponse {
        val roles = listOf(user.role?.name ?: "USER")

        // Tạo access token
        val accessToken = jwtUtil.generateAccessToken(
            userId = user.id,
            username = user.username,
            roles = roles
        )

        // Tạo refresh token
        val refreshTokenValue = jwtUtil.generateRefreshToken(user.id)

        // Lưu refresh token vào database
        val refreshToken = RefreshToken(
            user = user,
            token = refreshTokenValue,
            expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshToken.expiration / 1000),
            createdAt = LocalDateTime.now()
        )

        refreshTokenRepository.save(refreshToken)

        return JwtResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenValue,
            expiresIn = jwtProperties.accessToken.expiration,
            user = UserInfo(
                id = user.id,
                username = user.username,
                email = user.email,
                roles = roles
            )
        )
    }
}