package com.androidinsta.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.exposedHeaders = listOf("Authorization")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/posts/public/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/posts/advertise").permitAll()
                    .requestMatchers("/ws/**").permitAll() // WebSocket endpoint
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/api/search/**").permitAll() // Allow search endpoints
                    
                    // Admin endpoints
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    
                    // User endpoints
                    .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/posts/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/chat/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/comments/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/messages/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/notifications/**").hasAnyRole("USER", "ADMIN")
                    
                    // All other requests need authentication
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        // For H2 Console
        http.headers { headers ->
            headers.frameOptions { it.disable() }
        }

        return http.build()
    }
}