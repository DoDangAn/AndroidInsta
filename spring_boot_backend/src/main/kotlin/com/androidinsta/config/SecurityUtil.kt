package com.androidinsta.config

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
object SecurityUtil {

    @JvmStatic
    fun getCurrentUserId(): Long? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication?.isAuthenticated == true && authentication.principal != "anonymousUser") {
            authentication.principal as? Long
        } else {
            null
        }
    }

    @JvmStatic
    fun getCurrentUsername(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication?.isAuthenticated == true && authentication.principal != "anonymousUser") {
            when (val principal = authentication.principal) {
                is UserDetails -> principal.username
                is String -> principal
                else -> null
            }
        } else {
            null
        }
    }

    @JvmStatic
    fun hasRole(role: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.authorities?.any { 
            it.authority == "ROLE_$role" 
        } ?: false
    }

    @JvmStatic
    fun hasAnyRole(vararg roles: String): Boolean {
        return roles.any { hasRole(it) }
    }

    @JvmStatic
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.isAuthenticated == true && authentication.principal != "anonymousUser"
    }

    @JvmStatic
    fun isOwnerOrAdmin(resourceOwnerId: Long): Boolean {
        val currentUserId = getCurrentUserId()
        return currentUserId == resourceOwnerId || hasRole("ADMIN")
    }
}