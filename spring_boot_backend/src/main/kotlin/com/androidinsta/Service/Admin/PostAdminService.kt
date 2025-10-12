package com.androidinsta.service.admin

import com.androidinsta.model.Post
import com.androidinsta.repository.user.PostRepository
import org.springframework.stereotype.Service

@Service
class PostAdminService(private val postRepository: PostRepository) {

    fun getAllPosts(): List<Post> = postRepository.findAll()

    fun deletePost(postId: Long) {
        if (!postRepository.existsById(postId)) {
            throw RuntimeException("Post not found")
        }
        postRepository.deleteById(postId)
    }
}
