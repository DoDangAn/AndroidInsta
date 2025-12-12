package com.androidinsta.controller.User

import com.androidinsta.Service.NotificationService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.NotificationResponse
import com.androidinsta.dto.CountResponse
import com.androidinsta.dto.MessageResponse
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller cho notification operations
 * Handles retrieving, marking, and deleting notifications
 */
@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    /**
     * Lấy danh sách notifications
     * GET /api/notifications
     */
    @GetMapping
    fun getNotifications(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<NotificationResponse>> {
        val userId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        return ResponseEntity.ok(notificationService.getNotifications(userId, pageable))
    }

    /**
     * Lấy unread notifications
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    fun getUnreadNotifications(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<NotificationResponse>> {
        val userId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId, pageable))
    }

    /**
     * Đếm số unread notifications
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    fun getUnreadCount(): ResponseEntity<CountResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val count = notificationService.getUnreadCount(userId)
        return ResponseEntity.ok(
            CountResponse(
                success = true,
                count = count,
                message = "Unread notification count retrieved successfully"
            )
        )
    }

    /**
     * Đánh dấu notification là đã đọc
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    @CacheEvict(value = ["notifications", "unreadNotifications", "unreadCount"], allEntries = true)
    fun markAsRead(@PathVariable id: Long): ResponseEntity<MessageResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        notificationService.markAsRead(id, userId)
        return ResponseEntity.ok(MessageResponse("Marked as read"))
    }

    /**
     * Đánh dấu tất cả là đã đọc
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    @CacheEvict(value = ["notifications", "unreadNotifications", "unreadCount"], allEntries = true)
    fun markAllAsRead(): ResponseEntity<MessageResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        notificationService.markAllAsRead(userId)
        return ResponseEntity.ok(MessageResponse("All marked as read"))
    }

    /**
     * Xóa notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = ["notifications", "unreadNotifications", "unreadCount"], allEntries = true)
    fun deleteNotification(@PathVariable id: Long): ResponseEntity<MessageResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        notificationService.deleteNotification(id, userId)
        return ResponseEntity.ok(MessageResponse("Notification deleted"))
    }
}
