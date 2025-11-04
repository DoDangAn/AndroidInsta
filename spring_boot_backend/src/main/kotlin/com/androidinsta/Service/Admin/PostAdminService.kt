package com.androidinsta.Service.Admin

import com.androidinsta.Model.Post
import com.androidinsta.Repository.User.PostRepository
import org.springframework.stereotype.Service
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class PostAdminService(private val postRepository: PostRepository) {

    fun getAllPosts(): List<Post> = postRepository.findAll()

    fun deletePost(postId: Long) {
        if (!postRepository.existsById(postId)) {
            throw RuntimeException("Post not found")
        }
        postRepository.deleteById(postId)
    }
    
    fun searchPosts(keyword: String, pageable: Pageable): Page<Post> {
        return postRepository.searchPosts(keyword.trim(), pageable)
    }
}
