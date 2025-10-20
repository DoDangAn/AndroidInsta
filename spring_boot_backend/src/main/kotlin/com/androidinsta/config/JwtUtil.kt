package com.androidinsta.config

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtil(private val jwtProperties: JwtProperties) {

    private val key: Key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

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
            .signWith(key, SignatureAlgorithm.HS512)
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
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject.toLong()
    }

    fun getUsernameFromToken(token: String): String {
        val claims = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims["username"] as String
    }

    fun getRolesFromToken(token: String): List<String> {
        val claims = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        @Suppress("UNCHECKED_CAST")
        return claims["roles"] as List<String>
    }

    fun getTokenTypeFromToken(token: String): String {
        val claims = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims["type"] as String
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    fun getExpirationDateFromToken(token: String): Date {
        val claims = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims.expiration
    }
}