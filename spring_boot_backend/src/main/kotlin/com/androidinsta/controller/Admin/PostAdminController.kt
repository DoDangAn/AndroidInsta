package com.androidinsta.controller.admin

import com.androidinsta.model.Post
import com.androidinsta.service.admin.PostAdminService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/posts")
class `PostAdminController`(private val postAdminService: PostAdminService) {

    @GetMapping
    fun getAllPosts(): List<Post> = postAdminService.getAllPosts()

    @DeleteMapping("/{postId}")
    fun deletePost(@PathVariable postId: Long) = postAdminService.deletePost(postId)
}
