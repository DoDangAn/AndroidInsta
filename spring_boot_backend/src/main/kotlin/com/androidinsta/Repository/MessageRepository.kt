package com.androidinsta.Repository

import com.androidinsta.Model.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    
    /**
     * Lấy chat history giữa 2 users
     */
    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
           OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
        ORDER BY m.createdAt DESC
    """)
    fun findChatHistory(
        @Param("userId1") userId1: Long,
        @Param("userId2") userId2: Long,
        pageable: Pageable
    ): Page<Message>
    
    /**
     * Đếm số tin nhắn chưa đọc từ một user
     */
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.receiver.id = :receiverId 
          AND m.sender.id = :senderId
          AND m.isRead = false
    """)
    fun countUnreadMessages(
        @Param("receiverId") receiverId: Long,
        @Param("senderId") senderId: Long
    ): Long
    
    /**
     * Lấy tin nhắn cuối cùng giữa 2 users
     */
    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
           OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
        ORDER BY m.createdAt DESC
        LIMIT 1
    """)
    fun findLastMessage(
        @Param("userId1") userId1: Long,
        @Param("userId2") userId2: Long
    ): Message?
    
    /**
     * Lấy danh sách users đã chat với current user
     */
    @Query("""
        SELECT DISTINCT 
            CASE 
                WHEN m.sender.id = :userId THEN m.receiver.id
                ELSE m.sender.id
            END
        FROM Message m
        WHERE m.sender.id = :userId OR m.receiver.id = :userId
    """)
    fun findChatPartners(@Param("userId") userId: Long): List<Long>
    
    /**
     * Đánh dấu tất cả tin nhắn từ sender là đã đọc
     */
    @Query("""
        UPDATE Message m 
        SET m.isRead = true
        WHERE m.receiver.id = :receiverId 
          AND m.sender.id = :senderId
          AND m.isRead = false
    """)
    fun markMessagesAsRead(
        @Param("receiverId") receiverId: Long,
        @Param("senderId") senderId: Long
    )
}
