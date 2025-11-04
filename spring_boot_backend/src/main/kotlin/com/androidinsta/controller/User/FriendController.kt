package com.androidinsta.controller.User

import com.androidinsta.Service.FriendService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/friends")
class FriendController(
    private val friendService: FriendService
) {

    // Gửi friend request
    @PostMapping("/requests")
    fun sendFriendRequest(
        @RequestBody request: SendFriendRequestRequest
    ): ResponseEntity<FriendRequestResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = friendService.sendFriendRequest(userId, request.receiverId)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    // Lấy friend requests đã nhận
    @GetMapping("/requests/received")
    fun getReceivedFriendRequests(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<FriendRequestResponse>> {
        val userId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val result = friendService.getReceivedFriendRequests(userId, pageable)
        return ResponseEntity.ok(result)
    }

    // Lấy friend requests đã gửi
    @GetMapping("/requests/sent")
    fun getSentFriendRequests(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<FriendRequestResponse>> {
        val userId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val result = friendService.getSentFriendRequests(userId, pageable)
        return ResponseEntity.ok(result)
    }

    // Đếm pending requests
    @GetMapping("/requests/count")
    fun getPendingRequestsCount(): ResponseEntity<Map<String, Long>> {
        val userId = SecurityUtil.getCurrentUserId()
        val count = friendService.getPendingRequestsCount(userId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    // Chấp nhận friend request
    @PutMapping("/requests/{requestId}/accept")
    fun acceptFriendRequest(
        @PathVariable requestId: Long
    ): ResponseEntity<FriendRequestResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = friendService.acceptFriendRequest(requestId, userId)
        return ResponseEntity.ok(result)
    }

    // Từ chối friend request
    @PutMapping("/requests/{requestId}/reject")
    fun rejectFriendRequest(
        @PathVariable requestId: Long
    ): ResponseEntity<FriendRequestResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = friendService.rejectFriendRequest(requestId, userId)
        return ResponseEntity.ok(result)
    }

    // Hủy friend request đã gửi
    @DeleteMapping("/requests/{requestId}")
    fun cancelFriendRequest(
        @PathVariable requestId: Long
    ): ResponseEntity<Map<String, String>> {
        val userId = SecurityUtil.getCurrentUserId()
        friendService.cancelFriendRequest(requestId, userId)
        return ResponseEntity.ok(mapOf("message" to "Friend request cancelled"))
    }

    // Lấy danh sách bạn bè
    @GetMapping
    fun getFriends(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<FriendResponse>> {
        val userId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val result = friendService.getFriends(userId, pageable)
        return ResponseEntity.ok(result)
    }

    // Lấy bạn bè của user khác
    @GetMapping("/{userId}")
    fun getUserFriends(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<FriendResponse>> {
        val pageable = PageRequest.of(page, size)
        val result = friendService.getFriends(userId, pageable)
        return ResponseEntity.ok(result)
    }

    // Đếm số bạn bè
    @GetMapping("/count")
    fun getFriendsCount(): ResponseEntity<Map<String, Long>> {
        val userId = SecurityUtil.getCurrentUserId()
        val count = friendService.getFriendsCount(userId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    // Đếm số bạn bè của user khác
    @GetMapping("/{userId}/count")
    fun getUserFriendsCount(
        @PathVariable userId: Long
    ): ResponseEntity<Map<String, Long>> {
        val count = friendService.getFriendsCount(userId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    // Unfriend
    @DeleteMapping("/{friendId}")
    fun unfriend(
        @PathVariable friendId: Long
    ): ResponseEntity<Map<String, String>> {
        val userId = SecurityUtil.getCurrentUserId()
        friendService.unfriend(userId, friendId)
        return ResponseEntity.ok(mapOf("message" to "Unfriended successfully"))
    }

    // Lấy mutual friends (bạn chung)
    @GetMapping("/mutual/{userId}")
    fun getMutualFriends(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<FriendResponse>> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val result = friendService.getMutualFriends(currentUserId, userId, pageable)
        return ResponseEntity.ok(result)
    }

    // Lấy friend suggestions
    @GetMapping("/suggestions")
    fun getFriendSuggestions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<FriendSuggestionResponse>> {
        val userId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val result = friendService.getFriendSuggestions(userId, pageable)
        return ResponseEntity.ok(result)
    }

    // Kiểm tra friendship status với user khác
    @GetMapping("/status/{userId}")
    fun getFriendshipStatus(
        @PathVariable userId: Long
    ): ResponseEntity<FriendshipStatusResponse> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val result = friendService.getFriendshipStatus(currentUserId, userId)
        return ResponseEntity.ok(result)
    }
}
