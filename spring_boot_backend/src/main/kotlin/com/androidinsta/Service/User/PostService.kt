package com.androidinsta.service.user

import com.androidinsta.model.Post
import com.androidinsta.model.Visibility
import com.androidinsta.repository.user.PostRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PostService(private val postRepository: PostRepository) {

    fun getAllPosts(): List<Post> = postRepository.findAll()

    fun getPostsByUser(userId: Long): List<Post> =
        postRepository.findAllByUserId(userId)

    fun getPostById(postId: Long): Post =
        postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found with id $postId") }

    fun createPost(post: Post): Post = postRepository.save(post)

    fun updatePost(postId: Long, userId: Long, caption: String?, visibility: Visibility?): Post {
        val post = getPostById(postId)
        if (post.user.id != userId) throw RuntimeException("No permission to update this post")
        val updatedPost = post.copy(
            caption = caption ?: post.caption,
            visibility = visibility ?: post.visibility,
            updatedAt = LocalDateTime.now()
        )
        return postRepository.save(updatedPost)
    }

    fun deletePost(postId: Long, userId: Long) {
        val post = getPostById(postId)
        if (post.user.id != userId) throw RuntimeException("No permission to delete this post")
        postRepository.deleteById(postId)
    }
}
