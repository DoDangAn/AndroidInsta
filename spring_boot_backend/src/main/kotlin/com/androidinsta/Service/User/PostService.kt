package com.androidinsta.Service.User
import com.androidinsta.DTO.PostCreateRequest
import com.androidinsta.DTO.PostUpdateRequest
import com.androidinsta.DTO.PostResponse
import com.androidinsta.DTO.toPostResponse
import com.androidinsta.model.Post
import com.androidinsta.model.User
import com.androidinsta.model.Visibility
import com.androidinsta.repository.user.PostRepository
import com.androidinsta.repository.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PostService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {

    // Lấy tất cả bài viết, trả về DTO
    fun getAllPosts(): List<PostResponse> =
        postRepository.findAll().map { it.toPostResponse() }

    // Lấy bài viết theo userId, trả về DTO
    fun getPostsByUser(userId: Long): List<PostResponse> =
        postRepository.findAllByUserId(userId).map { it.toPostResponse() }

    // Lấy 1 bài viết theo id, trả về DTO
    fun getPostById(postId: Long): PostResponse =
        postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found with id $postId") }
            .toPostResponse()

    // Tạo post mới, trả về DTO
    fun createPost(request: PostCreateRequest): Post {
        // Lấy User từ userId
        val user: User = userRepository.findById(request.userId)
            .orElseThrow { RuntimeException("User not found with id ${request.userId}") }

        val post = Post(
            user = user,
            caption = request.caption,
            visibility = request.visibility ?: Visibility.PUBLIC,
            createdAt = LocalDateTime.now()
        )

        return postRepository.save(post)
    }


    // Update post, trả về DTO
    fun updatePost(postId: Long, userId: Long, caption: String?, visibility: Visibility?): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found with id $postId") }

        if (post.user.id != userId) throw RuntimeException("No permission to update this post")

        val updatedPost = post.copy(
            caption = caption ?: post.caption,
            visibility = visibility ?: post.visibility,
            updatedAt = LocalDateTime.now()
        )

        return postRepository.save(updatedPost).toPostResponse()
    }

    // Xóa post
    fun deletePost(postId: Long, userId: Long) {
        val post = postRepository.findById(postId)
            .orElseThrow { RuntimeException("Post not found with id $postId") }

        if (post.user.id != userId) throw RuntimeException("No permission to delete this post")

        postRepository.deleteById(postId)
    }
}
