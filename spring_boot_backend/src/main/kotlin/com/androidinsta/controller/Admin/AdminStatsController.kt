package com.androidinsta.controller.Admin

import com.androidinsta.Service.Admin.AdminStatsService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
class AdminStatsController(private val adminStatsService: AdminStatsService) {

    /**
     * Get overall system statistics
     * GET /api/admin/stats/overview
     */
    @GetMapping("/overview")
    @org.springframework.cache.annotation.Cacheable(
        value = ["adminOverviewStats"],
        key = "'overview'"
    )
    fun getOverviewStats(): ResponseEntity<Map<String, Any>> {
        val stats = adminStatsService.getOverviewStats()
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "stats" to stats
            )
        )
    }

    /**
     * Get user statistics (registrations over time, active users, etc.)
     * GET /api/admin/stats/users?period=7d
     */
    @GetMapping("/users")
    @org.springframework.cache.annotation.Cacheable(
        value = ["adminUserStats"],
        key = "#period"
    )
    fun getUserStats(
        @RequestParam(required = false, defaultValue = "7d") period: String
    ): ResponseEntity<Map<String, Any>> {
        val stats = adminStatsService.getUserStats(period)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "stats" to stats
            )
        )
    }

    /**
     * Get post statistics (posts over time, media types, etc.)
     * GET /api/admin/stats/posts?period=7d
     */
    @GetMapping("/posts")
    @org.springframework.cache.annotation.Cacheable(
        value = ["adminPostStats"],
        key = "#period"
    )
    fun getPostStats(
        @RequestParam(required = false, defaultValue = "7d") period: String
    ): ResponseEntity<Map<String, Any>> {
        val stats = adminStatsService.getPostStats(period)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "stats" to stats
            )
        )
    }

    /**
     * Get engagement statistics (likes, comments, follows)
     * GET /api/admin/stats/engagement?period=7d
     */
    @GetMapping("/engagement")
    @org.springframework.cache.annotation.Cacheable(
        value = ["adminEngagementStats"],
        key = "#period"
    )
    fun getEngagementStats(
        @RequestParam(required = false, defaultValue = "7d") period: String
    ): ResponseEntity<Map<String, Any>> {
        val stats = adminStatsService.getEngagementStats(period)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "stats" to stats
            )
        )
    }

    /**
     * Get top users (most followers, most posts, most active)
     * GET /api/admin/stats/top-users?type=followers&limit=10
     */
    @GetMapping("/top-users")
    @org.springframework.cache.annotation.Cacheable(
        value = ["adminTopUsers"],
        key = "#type + '_limit_' + #limit"
    )
    fun getTopUsers(
        @RequestParam(required = false, defaultValue = "followers") type: String,
        @RequestParam(required = false, defaultValue = "10") limit: Int
    ): ResponseEntity<Map<String, Any>> {
        val topUsers = adminStatsService.getTopUsers(type, limit)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "topUsers" to topUsers
            )
        )
    }

    /**
     * Get top posts (most liked, most commented)
     * GET /api/admin/stats/top-posts?type=likes&limit=10
     */
    @GetMapping("/top-posts")
    @org.springframework.cache.annotation.Cacheable(
        value = ["adminTopPosts"],
        key = "#type + '_limit_' + #limit"
    )
    fun getTopPosts(
        @RequestParam(required = false, defaultValue = "likes") type: String,
        @RequestParam(required = false, defaultValue = "10") limit: Int
    ): ResponseEntity<Map<String, Any>> {
        val topPosts = adminStatsService.getTopPosts(type, limit)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "topPosts" to topPosts
            )
        )
    }
}
