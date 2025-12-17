package com.androidinsta.config


import com.androidinsta.Service.RedisService
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val redisService: RedisService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            try {
                // Check if token is blacklisted (logged out or revoked)
                if (redisService.isTokenBlacklisted(token)) {
                    logger.warn("Token is blacklisted: ${token.take(20)}...")
                    filterChain.doFilter(request, response)
                    return
                }

                logger.debug("Authorization header: $authHeader")
                logger.debug("Token extracted: $token")

                // Add detailed logs for debugging
                logger.debug("Validating token: $token")
                val isValid = jwtUtil.validateToken(token)
                logger.debug("Token validation result: $isValid")

                val isExpired = jwtUtil.isTokenExpired(token)
                logger.debug("Token expiration check: $isExpired")

                if (isValid && !isExpired) {
                    val tokenType = jwtUtil.getTokenTypeFromToken(token)
                    logger.debug("Token type: $tokenType")

                    // Chỉ cho phép access token để authentication
                    if (tokenType == "access") {
                        val userId = jwtUtil.getUserIdFromToken(token)
                        logger.debug("Extracted userId: $userId")

                        val roles = jwtUtil.getRolesFromToken(token)
                        logger.debug("Extracted roles: $roles")

                        // Spring Security expects roles with ROLE_ prefix
                        val authorities = roles.map { role: String ->
                            val roleUpper = role.uppercase()
                            val roleWithPrefix = if (roleUpper.startsWith("ROLE_")) roleUpper else "ROLE_$roleUpper"
                            SimpleGrantedAuthority(roleWithPrefix)
                        }
                        logger.debug("Mapped authorities: ${authorities.map { authority -> authority.authority }}")

                        val authentication = UsernamePasswordAuthenticationToken(
                            userId, // principal
                            null, // credentials
                            authorities
                        )

                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication

                        logger.info("Authenticated user: userId=$userId, originalRoles=$roles, authorities=${authorities.map { authority -> authority.authority }}")
                    } else {
                        logger.warn("Invalid token type: $tokenType")
                    }
                } else {
                    logger.warn("Token is invalid or expired")
                }
            } catch (e: JwtException) {
                logger.error("JWT processing error: ${e.message}", e)
            } catch (e: IllegalArgumentException) {
                logger.error("Invalid token format: ${e.message}", e)
            } catch (e: Exception) {
                logger.error("Unexpected error during token processing: ${e.message}", e)
            }
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }
}
