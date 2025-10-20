package com.androidinsta.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secret: String = "",
    var issuer: String = "",
    var audience: String = ""
) {
    var accessToken = TokenProperties()
    var refreshToken = TokenProperties()

    data class TokenProperties(
        var expiration: Long = 0
    )
}