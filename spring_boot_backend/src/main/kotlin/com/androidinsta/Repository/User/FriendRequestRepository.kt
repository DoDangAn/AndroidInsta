package com.androidinsta.Repository.User

import com.androidinsta.Model.FriendRequest
import com.androidinsta.Model.FriendRequestStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FriendRequestRepository : JpaRepository<FriendRequest, Long> {
    
    // Kiểm tra có friend request giữa 2 user không
    @Query("""
        SELECT fr FROM FriendRequest fr 
        WHERE ((fr.sender.id = :userId1 AND fr.receiver.id = :userId2) 
            OR (fr.sender.id = :userId2 AND fr.receiver.id = :userId1))
        AND fr.status = :status
    """)
    fun findBetweenUsers(userId1: Long, userId2: Long, status: FriendRequestStatus): FriendRequest?

    // Lấy tất cả friend requests đã nhận (PENDING)
    @Query("""
        SELECT fr FROM FriendRequest fr 
        WHERE fr.receiver.id = :userId 
        AND fr.status = 'PENDING'
        ORDER BY fr.createdAt DESC
    """)
    fun findPendingReceivedRequests(userId: Long, pageable: Pageable): Page<FriendRequest>

    // Lấy tất cả friend requests đã gửi (PENDING)
    @Query("""
        SELECT fr FROM FriendRequest fr 
        WHERE fr.sender.id = :userId 
        AND fr.status = 'PENDING'
        ORDER BY fr.createdAt DESC
    """)
    fun findPendingSentRequests(userId: Long, pageable: Pageable): Page<FriendRequest>

    // Đếm số pending requests
    fun countByReceiverIdAndStatus(receiverId: Long, status: FriendRequestStatus): Long

    // Tìm request cụ thể
    @Query("""
        SELECT fr FROM FriendRequest fr 
        WHERE fr.sender.id = :senderId 
        AND fr.receiver.id = :receiverId 
        AND fr.status = :status
    """)
    fun findBySenderAndReceiver(senderId: Long, receiverId: Long, status: FriendRequestStatus): FriendRequest?

    // Xóa tất cả requests giữa 2 users
    @Query("""
        DELETE FROM FriendRequest fr 
        WHERE (fr.sender.id = :userId1 AND fr.receiver.id = :userId2)
        OR (fr.sender.id = :userId2 AND fr.receiver.id = :userId1)
    """)
    fun deleteAllBetweenUsers(userId1: Long, userId2: Long)
}
