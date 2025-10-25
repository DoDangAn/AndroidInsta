package com.androidinsta.config

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil(private val jwtProperties: JwtProperties) {

    // Khởi tạo key: nếu có secret trong properties thì dùng đó (hmacShaKeyFor),
    // nếu không có thì fallback sang tạo key ngẫu nhiên
    private val key: javax.crypto.SecretKey = if (jwtProperties.secret.isNotBlank()) {
        // Use HMAC SHA key derived from configured secret (minimum 512 bits for HS512)
        val secretBytes = jwtProperties.secret.toByteArray(Charsets.UTF_8)
        if (secretBytes.size < 64) {
            // Pad secret to minimum 64 bytes (512 bits) for HS512
            Keys.hmacShaKeyFor(secretBytes.copyOf(64))
        } else {
            Keys.hmacShaKeyFor(secretBytes)
        }
    } else {
        // Generate secure random key
        Jwts.SIG.HS512.key().build()
    }

    fun generateAccessToken(userId: Long, username: String, roles: List<String>): String {
        val claimsMap: MutableMap<String, Any> = HashMap()
        claimsMap["sub"] = userId.toString()
        claimsMap["username"] = username
        claimsMap["roles"] = roles
        claimsMap["type"] = "access"

        return Jwts.builder()
            .setClaims(claimsMap)
            .setIssuer(jwtProperties.issuer)
            .setAudience(jwtProperties.audience)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtProperties.accessToken.expiration))
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(userId: Long): String {
        val claimsMap: MutableMap<String, Any> = HashMap()
        claimsMap["sub"] = userId.toString()
        claimsMap["type"] = "refresh"

        return Jwts.builder()
            .setClaims(claimsMap)
            .setIssuer(jwtProperties.issuer)
            .setAudience(jwtProperties.audience)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtProperties.refreshToken.expiration))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key as javax.crypto.SecretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parser()
            .verifyWith(key as javax.crypto.SecretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims.subject.toLong()
    }

    fun getUsernameFromToken(token: String): String {
        val claims = Jwts.parser()
            .verifyWith(key as javax.crypto.SecretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims["username"] as String
    }

    fun getRolesFromToken(token: String): List<String> {
        val claims = Jwts.parser()
            .verifyWith(key as javax.crypto.SecretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        @Suppress("UNCHECKED_CAST")
        return claims["roles"] as List<String>
    }

    fun getTokenTypeFromToken(token: String): String {
        val claims = Jwts.parser()
            .verifyWith(key as javax.crypto.SecretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims["type"] as String
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key as javax.crypto.SecretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    fun getExpirationDateFromToken(token: String): Date {
        val claims = Jwts.parser()
            .verifyWith(key as javax.crypto.SecretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims.expiration
    }
}
