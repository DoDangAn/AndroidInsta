package com.androidinsta.config

import com.androidinsta.Service.RedisService
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

                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                    val tokenType = jwtUtil.getTokenTypeFromToken(token)
                    
                    // Chỉ cho phép access token để authentication
                    if (tokenType == "access") {
                        val userId = jwtUtil.getUserIdFromToken(token)
                        val roles = jwtUtil.getRolesFromToken(token)

                        val authorities = roles.map { SimpleGrantedAuthority(it) }

                        val authentication = UsernamePasswordAuthenticationToken(
                            userId, // principal
                            null, // credentials
                            authorities
                        )

                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                        
                        logger.debug("Authenticated user: userId=$userId, roles=$roles")
                    }
                }
            } catch (e: Exception) {
                // Token invalid or parsing error; log for debugging and continue without authentication
                logger.warn("Invalid JWT token", e)
            }
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }
}
