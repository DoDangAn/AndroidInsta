package com.androidinsta.config

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

@Component
object SecurityUtil {

    private val logger = LoggerFactory.getLogger(SecurityUtil::class.java)

    @JvmStatic
    fun getCurrentUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        logger.debug("Authentication object: $authentication")

        if (authentication?.isAuthenticated == true && authentication.principal != "anonymousUser") {
            val principal = authentication.principal
            logger.debug("Principal type: ${principal::class.java}, Principal value: $principal")

            return when (principal) {
                is Long -> principal
                is UserDetails -> principal.username.toLongOrNull() ?: throw IllegalStateException("User ID not found in UserDetails username. Principal: $principal")
                is String -> principal.toLongOrNull() ?: throw IllegalStateException("User ID not found in String principal. Principal: $principal")
                else -> throw IllegalStateException("Unsupported principal type: ${principal::class.java}. Principal: $principal")
            }
        } else {
            logger.warn("User not authenticated. Returning -1 as default user ID.")
            return -1 // Default value for unauthenticated users
        }
    }

    @JvmStatic
    fun getCurrentUserIdOrNull(): Long? {
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