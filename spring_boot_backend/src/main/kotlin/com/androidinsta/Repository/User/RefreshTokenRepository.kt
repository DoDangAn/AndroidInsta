package com.androidinsta.Repository.User

import com.androidinsta.Model.RefreshToken
import com.androidinsta.Model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    
    fun findByToken(token: String): Optional<RefreshToken>
    
    fun findByUserAndRevokedFalse(user: User): List<RefreshToken>
    
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    fun revokeAllUserTokens(user: User): Int
    
    @Modifying
    @Transactional
    fun deleteByUser(user: User): Int
}