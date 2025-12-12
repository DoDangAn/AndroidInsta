package com.androidinsta.controller.Admin

import com.androidinsta.Service.Admin.AdminStatsService
import com.androidinsta.dto.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * REST controller cho admin statistics
 * Provides system-wide statistics for admin dashboard
 * Requires ADMIN role for all endpoints
 */
@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
class AdminStatsController(private val adminStatsService: AdminStatsService) {

    /**
     * Lấy overall system statistics
     * GET /api/admin/stats/overview
     */
    @GetMapping("/overview")
    fun getOverviewStats(): ResponseEntity<AdminStatsResponse> {
        val stats = adminStatsService.getOverviewStats()
        return ResponseEntity.ok(
            AdminStatsResponse(
                success = true,
                message = "Overview stats retrieved successfully",
                data = stats
            )
        )
    }

    /**
     * Lấy user statistics
     * GET /api/admin/stats/users?period=7d
     */
    @GetMapping("/users")
    fun getUserStats(@RequestParam(defaultValue = "7d") period: String): ResponseEntity<AdminStatsResponse> {
        val stats = adminStatsService.getUserStats(period)
        return ResponseEntity.ok(
            AdminStatsResponse(
                success = true,
                message = "User stats retrieved successfully",
                data = stats
            )
        )
    }

    /**
     * Lấy post statistics
     * GET /api/admin/stats/posts?period=7d
     */
    @GetMapping("/posts")
    fun getPostStats(@RequestParam(defaultValue = "7d") period: String): ResponseEntity<AdminStatsResponse> {
        val stats = adminStatsService.getPostStats(period)
        return ResponseEntity.ok(
            AdminStatsResponse(
                success = true,
                message = "Post stats retrieved successfully",
                data = stats
            )
        )
    }

    /**
     * Lấy engagement statistics
     * GET /api/admin/stats/engagement?period=7d
     */
    @GetMapping("/engagement")
    fun getEngagementStats(@RequestParam(defaultValue = "7d") period: String): ResponseEntity<AdminStatsResponse> {
        val stats = adminStatsService.getEngagementStats(period)
        return ResponseEntity.ok(
            AdminStatsResponse(
                success = true,
                message = "Engagement stats retrieved successfully",
                data = stats
            )
        )
    }

    /**
     * Lấy top users
     * GET /api/admin/stats/top-users?type=followers&limit=10
     */
    @GetMapping("/top-users")
    fun getTopUsers(
        @RequestParam(defaultValue = "followers") type: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<Map<String, Any>> {
        val topUsers = adminStatsService.getTopUsers(type, limit)
        return ResponseEntity.ok(mapOf("success" to true, "topUsers" to topUsers))
    }

    /**
     * Lấy top posts
     * GET /api/admin/stats/top-posts?type=likes&limit=10
     */
    @GetMapping("/top-posts")
    fun getTopPosts(
        @RequestParam(defaultValue = "likes") type: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<Map<String, Any>> {
        val topPosts = adminStatsService.getTopPosts(type, limit)
        return ResponseEntity.ok(mapOf("success" to true, "topPosts" to topPosts))
    }
}
