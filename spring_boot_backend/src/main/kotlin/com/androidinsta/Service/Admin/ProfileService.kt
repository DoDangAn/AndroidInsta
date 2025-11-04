package com.androidinsta.Service.Admin

import com.androidinsta.Repository.User.UserRepository
import com.androidinsta.dto.ProfileDto
import com.androidinsta.dto.toProfileDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProfileService(
    private val userRepository: UserRepository
) {

    fun getUserProfile(userId: Long): ProfileDto {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found with id: $userId") }
        return user.toProfileDto()
    }
}
