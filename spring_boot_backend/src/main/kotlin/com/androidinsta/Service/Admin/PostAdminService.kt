package com.androidinsta.Service.Admin

import com.androidinsta.Model.Post
import com.androidinsta.Repository.User.PostRepository
import org.springframework.stereotype.Service
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class PostAdminService(
    private val postRepository: PostRepository,
    private val kafkaProducerService: com.androidinsta.Service.KafkaProducerService,
    private val redisService: com.androidinsta.Service.RedisService
) {

    fun getAllPosts(): List<Post> = postRepository.findAll()

    fun deletePost(postId: Long) {
        if (!postRepository.existsById(postId)) {
            throw RuntimeException("Post not found")
        }
        
        val post = postRepository.findById(postId).get()
        val userId = post.user.id
        
        postRepository.deleteById(postId)
        
        // Send Kafka event for audit trail
        kafkaProducerService.sendAdminPostDeletedEvent(postId, userId)
        
        // Invalidate caches
        redisService.delete("feed:posts:*")
        redisService.delete("user:posts:$userId:*")
        redisService.invalidateUserCache(userId)
    }
    
    fun searchPosts(keyword: String, pageable: Pageable): Page<Post> {
        return postRepository.searchPosts(keyword.trim(), pageable)
    }
}
