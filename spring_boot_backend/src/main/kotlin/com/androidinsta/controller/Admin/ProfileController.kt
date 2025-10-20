package com.androidinsta.controller.Admin

import com.androidinsta.Service.Admin.ProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/profile")
class ProfileController(private val profileService: ProfileService) {

    @GetMapping("/{userId}")
    fun getUserProfile(@PathVariable userId: Long): ResponseEntity<*> {
        val userProfile = profileService.getUserProfile(userId)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to userProfile
            )
        )
    }
}
