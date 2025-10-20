package com.androidinsta.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil
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
                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                    val tokenType = jwtUtil.getTokenTypeFromToken(token)
                    
                    // Chỉ cho phép access token để authentication
                    if (tokenType == "access") {
                        val userId = jwtUtil.getUserIdFromToken(token)
                        val username = jwtUtil.getUsernameFromToken(token)
                        val roles = jwtUtil.getRolesFromToken(token)

                        val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }

                        val authentication = UsernamePasswordAuthenticationToken(
                            userId, // principal
                            null, // credentials
                            authorities
                        )

                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            } catch (e: Exception) {
                // Token invalid, continue without authentication
                logger.debug("JWT token validation failed", e)
            }
        }

        filterChain.doFilter(request, response)
    }
}