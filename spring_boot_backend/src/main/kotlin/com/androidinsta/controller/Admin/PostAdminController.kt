package com.androidinsta.controller.Admin

import com.androidinsta.Model.Post
import com.androidinsta.Service.Admin.PostAdminService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * REST controller cho admin post management
 * Handles post administration: view, search, delete posts
 * Requires ADMIN role for all endpoints
 */
@RestController
@RequestMapping("/api/admin/posts")
@PreAuthorize("hasRole('ADMIN')")
class PostAdminController(private val postAdminService: PostAdminService) {

    /**
     * Lấy tất cả posts
     * GET /api/admin/posts
     */
    @GetMapping
    fun getAllPosts(): List<Post> = postAdminService.getAllPosts()

    /**
     * Tìm kiếm posts
     * GET /api/admin/posts/search?keyword=vacation&page=0&size=10&sortBy=createdAt&direction=DESC
     */
    @GetMapping("/search")
    fun searchPosts(
        @RequestParam(defaultValue = "") keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") direction: String
    ): Page<Post> {
        val sortDir = if (direction.equals("DESC", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy))
        return postAdminService.searchPosts(keyword, pageable)
    }

    /**
     * Xóa post
     * DELETE /api/admin/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    @CacheEvict(value = ["adminAllPosts", "adminSearchPosts", "feedPosts", "userPosts"], allEntries = true)
    fun deletePost(@PathVariable postId: Long) = postAdminService.deletePost(postId)
}
