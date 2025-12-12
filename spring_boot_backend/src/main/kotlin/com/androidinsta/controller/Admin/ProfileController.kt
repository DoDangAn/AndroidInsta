package com.androidinsta.controller.Admin

import com.androidinsta.Service.Admin.ProfileService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * REST controller cho admin profile viewing
 * Allows admins to view user profiles
 * Requires ADMIN role
 */
@RestController
@RequestMapping("/api/admin/profile")
@PreAuthorize("hasRole('ADMIN')")
class ProfileController(private val profileService: ProfileService) {

    /**
     * Lấy user profile by ID
     * GET /api/admin/profile/{userId}
     */
    @GetMapping("/{userId}")
    fun getUserProfile(@PathVariable userId: Long): ResponseEntity<*> {
        val userProfile = profileService.getUserProfile(userId)
        return ResponseEntity.ok(mapOf("success" to true, "data" to userProfile))
    }
}
