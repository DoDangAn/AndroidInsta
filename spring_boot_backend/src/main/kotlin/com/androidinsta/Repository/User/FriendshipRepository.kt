package com.androidinsta.Repository.User

import com.androidinsta.Model.Friendship
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FriendshipRepository : JpaRepository<Friendship, Long> {
    
    // Kiểm tra 2 users có phải bạn bè không
    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END 
        FROM Friendship f 
        WHERE f.user.id = :userId AND f.friend.id = :friendId
    """)
    fun areFriends(userId: Long, friendId: Long): Boolean

    // Lấy danh sách bạn bè
    @Query("""
        SELECT f FROM Friendship f 
        WHERE f.user.id = :userId 
        ORDER BY f.createdAt DESC
    """)
    fun findFriends(userId: Long, pageable: Pageable): Page<Friendship>

    // Lấy tất cả friend IDs (không phân trang)
    @Query("""
        SELECT f.friend.id FROM Friendship f 
        WHERE f.user.id = :userId
    """)
    fun findFriendIds(userId: Long): List<Long>

    // Đếm số bạn bè
    fun countByUserId(userId: Long): Long

    // Tìm mutual friends (bạn chung)
    @Query("""
        SELECT f FROM Friendship f 
        WHERE f.user.id = :userId1 
        AND f.friend.id IN (
            SELECT f2.friend.id FROM Friendship f2 
            WHERE f2.user.id = :userId2
        )
    """)
    fun findMutualFriends(userId1: Long, userId2: Long, pageable: Pageable): Page<Friendship>

    // Đếm mutual friends
    @Query("""
        SELECT COUNT(f) FROM Friendship f 
        WHERE f.user.id = :userId1 
        AND f.friend.id IN (
            SELECT f2.friend.id FROM Friendship f2 
            WHERE f2.user.id = :userId2
        )
    """)
    fun countMutualFriends(userId1: Long, userId2: Long): Long

    // Xóa friendship
    @Modifying
    @Query("""
        DELETE FROM Friendship f 
        WHERE f.user.id = :userId AND f.friend.id = :friendId
    """)
    fun deleteFriendship(userId: Long, friendId: Long)

    // Tìm friendship suggestions (bạn của bạn bè chưa kết bạn)
    @Query("""
        SELECT DISTINCT f2.friend FROM Friendship f1
        JOIN Friendship f2 ON f1.friend.id = f2.user.id
        WHERE f1.user.id = :userId
        AND f2.friend.id != :userId
        AND f2.friend.id NOT IN (
            SELECT f3.friend.id FROM Friendship f3 WHERE f3.user.id = :userId
        )
        AND f2.friend.id NOT IN (
            SELECT fr.receiver.id FROM FriendRequest fr 
            WHERE fr.sender.id = :userId AND fr.status = 'PENDING'
        )
        AND f2.friend.id NOT IN (
            SELECT fr.sender.id FROM FriendRequest fr 
            WHERE fr.receiver.id = :userId AND fr.status = 'PENDING'
        )
        ORDER BY f2.createdAt DESC
    """)
    fun findFriendSuggestions(userId: Long, pageable: Pageable): Page<com.androidinsta.Model.User>
}
