package com.androidinsta.controller.User

import com.androidinsta.Service.NotificationService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.NotificationResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    /**
     * Lấy danh sách notifications
     */
    @org.springframework.cache.annotation.Cacheable(value = ["notifications"], key = "#userId + '_page_' + #page + '_size_' + #size")
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
     */
    @org.springframework.cache.annotation.Cacheable(value = ["unreadNotifications"], key = "#userId + '_page_' + #page + '_size_' + #size")
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
     */
    @org.springframework.cache.annotation.Cacheable(value = ["unreadCount"], key = "#userId")
    @GetMapping("/unread/count")
    fun getUnreadCount(): ResponseEntity<Map<String, Long>> {
        val userId = SecurityUtil.getCurrentUserId()
        val count = notificationService.getUnreadCount(userId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    /**
     * Đánh dấu notification là đã đọc
     */
    @org.springframework.cache.annotation.CacheEvict(value = ["notifications", "unreadNotifications", "unreadCount"], allEntries = true)
    @PutMapping("/{id}/read")
    fun markAsRead(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        val userId = SecurityUtil.getCurrentUserId()
        notificationService.markAsRead(id, userId)
        return ResponseEntity.ok(mapOf("message" to "Marked as read"))
    }

    /**
     * Đánh dấu tất cả là đã đọc
     */
    @org.springframework.cache.annotation.CacheEvict(value = ["notifications", "unreadNotifications", "unreadCount"], allEntries = true)
    @PutMapping("/read-all")
    fun markAllAsRead(): ResponseEntity<Map<String, String>> {
        val userId = SecurityUtil.getCurrentUserId()
        notificationService.markAllAsRead(userId)
        return ResponseEntity.ok(mapOf("message" to "All marked as read"))
    }

    /**
     * Xóa notification
     */
    @org.springframework.cache.annotation.CacheEvict(value = ["notifications", "unreadNotifications", "unreadCount"], allEntries = true)
    @DeleteMapping("/{id}")
    fun deleteNotification(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        val userId = SecurityUtil.getCurrentUserId()
        notificationService.deleteNotification(id, userId)
        return ResponseEntity.ok(mapOf("message" to "Notification deleted"))
    }
}
