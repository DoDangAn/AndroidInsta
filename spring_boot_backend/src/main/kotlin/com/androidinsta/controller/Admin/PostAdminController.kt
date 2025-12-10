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

    @org.springframework.cache.annotation.Cacheable(value = ["adminAllPosts"], key = "'all'")
    @GetMapping
    fun getAllPosts(): List<Post> = postAdminService.getAllPosts()

    @org.springframework.cache.annotation.Cacheable(value = ["adminSearchPosts"], key = "#keyword + '_page_' + #page + '_size_' + #size + '_sort_' + #sortBy + '_' + #direction")
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

    @org.springframework.cache.annotation.CacheEvict(value = ["adminAllPosts", "adminSearchPosts", "feedPosts", "userPosts"], allEntries = true)
    @DeleteMapping("/{postId}")
    fun deletePost(@PathVariable postId: Long) = postAdminService.deletePost(postId)

}
