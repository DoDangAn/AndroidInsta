package com.androidinsta.controller.User

import com.androidinsta.Service.FriendService
import com.androidinsta.config.SecurityUtil
import com.androidinsta.dto.*
import jakarta.validation.Valid
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller cho friend operations
 * Handles friend requests, friends list, and friendship management
 */
@RestController
@RequestMapping("/api/friends")
class FriendController(
    private val friendService: FriendService
) {

    /**
     * Gửi friend request
     * POST /api/friends/requests
     */
    @PostMapping("/requests")
    fun sendFriendRequest(@Valid @RequestBody request: SendFriendRequestRequest): ResponseEntity<FriendRequestResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = friendService.sendFriendRequest(userId, request.receiverId)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    /**
     * Lấy friend requests đã nhận
     * GET /api/friends/requests/received
     */
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

    /**
     * Lấy friend requests đã gửi
     * GET /api/friends/requests/sent
     */
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

    /**
     * Đếm pending requests
     * GET /api/friends/requests/count
     */
    @GetMapping("/requests/count")
    fun getPendingRequestsCount(): ResponseEntity<CountResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val count = friendService.getPendingRequestsCount(userId)
        return ResponseEntity.ok(
            CountResponse(
                success = true,
                count = count,
                message = "Pending requests count retrieved successfully"
            )
        )
    }

    /**
     * Chấp nhận friend request
     * PUT /api/friends/requests/{requestId}/accept
     */
    @PutMapping("/requests/{requestId}/accept")
    @CacheEvict(value = ["friendRequests", "friendsList"], allEntries = true)
    fun acceptFriendRequest(@PathVariable requestId: Long): ResponseEntity<FriendRequestResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = friendService.acceptFriendRequest(requestId, userId)
        return ResponseEntity.ok(result)
    }

    /**
     * Từ chối friend request
     * PUT /api/friends/requests/{requestId}/reject
     */
    @PutMapping("/requests/{requestId}/reject")
    fun rejectFriendRequest(@PathVariable requestId: Long): ResponseEntity<FriendRequestResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = friendService.rejectFriendRequest(requestId, userId)
        return ResponseEntity.ok(result)
    }

    /**
     * Hủy friend request đã gửi
     * DELETE /api/friends/requests/{requestId}
     */
    @DeleteMapping("/requests/{requestId}")
    fun cancelFriendRequest(@PathVariable requestId: Long): ResponseEntity<MessageResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        friendService.cancelFriendRequest(requestId, userId)
        return ResponseEntity.ok(MessageResponse("Friend request cancelled"))
    }

    /**
     * Lấy danh sách bạn bè
     * GET /api/friends
     */
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

    /**
     * Lấy bạn bè của user khác
     * GET /api/friends/{userId}
     */
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

    /**
     * Đếm số bạn bè
     * GET /api/friends/count
     */
    @GetMapping("/count")
    fun getFriendsCount(): ResponseEntity<CountResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val count = friendService.getFriendsCount(userId)
        return ResponseEntity.ok(
            CountResponse(
                success = true,
                count = count,
                message = "Friends count retrieved successfully"
            )
        )
    }

    /**
     * Đếm số bạn bè của user khác
     * GET /api/friends/{userId}/count
     */
    @GetMapping("/{userId}/count")
    fun getUserFriendsCount(@PathVariable userId: Long): ResponseEntity<CountResponse> {
        val count = friendService.getFriendsCount(userId)
        return ResponseEntity.ok(
            CountResponse(
                success = true,
                count = count,
                message = "User friends count retrieved successfully"
            )
        )
    }

    /**
     * Unfriend
     * DELETE /api/friends/{friendId}
     */
    @DeleteMapping("/{friendId}")
    fun unfriend(@PathVariable friendId: Long): ResponseEntity<MessageResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        friendService.unfriend(userId, friendId)
        return ResponseEntity.ok(MessageResponse("Unfriended successfully"))
    }

    /**
     * Lấy mutual friends (bạn chung)
     * GET /api/friends/mutual/{userId}
     */
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

    /**
     * Lấy friend suggestions
     * GET /api/friends/suggestions
     */
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

    /**
     * Kiểm tra friendship status với user khác
     * GET /api/friends/status/{userId}
     */
    @GetMapping("/status/{userId}")
    fun getFriendshipStatus(@PathVariable userId: Long): ResponseEntity<FriendshipStatusResponse> {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val result = friendService.getFriendshipStatus(currentUserId, userId)
        return ResponseEntity.ok(result)
    }
}
