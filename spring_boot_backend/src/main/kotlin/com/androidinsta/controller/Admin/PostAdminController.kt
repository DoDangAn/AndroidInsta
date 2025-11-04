package com.androidinsta.controller.admin

import com.androidinsta.Model.Post
import com.androidinsta.Service.Admin.PostAdminService
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/api/admin/posts")
class PostAdminController(private val postAdminService: PostAdminService) {

    @GetMapping
    fun getAllPosts(): List<Post> = postAdminService.getAllPosts()

    @GetMapping("/search")
    fun searchPosts(
        @RequestParam(required = false, defaultValue = "") keyword: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "createdAt") sortBy: String,
        @RequestParam(required = false, defaultValue = "DESC") direction: String
    ): Page<Post> {
        val sortDir = if (direction.equals("DESC", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy))
        return postAdminService.searchPosts(keyword, pageable)
    }

    @DeleteMapping("/{postId}")
    fun deletePost(@PathVariable postId: Long) = postAdminService.deletePost(postId)

}
