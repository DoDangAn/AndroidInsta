package com.androidinsta.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var issuer: String = "",
    var audience: String = "",
    var secret: String = "",
    var accessTokenExpiration: Long = 0,
    var refreshTokenExpiration: Long = 0,
) {
    val accessToken: TokenProperties
        get() = TokenProperties(accessTokenExpiration)
    val refreshToken: TokenProperties
        get() = TokenProperties(refreshTokenExpiration)


    data class TokenProperties(
        var expiration: Long = 0
    )
}