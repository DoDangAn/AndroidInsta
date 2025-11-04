package com.androidinsta.Repository.User

import com.androidinsta.Model.Notification
import com.androidinsta.Model.NotificationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    
    /**
     * Lấy tất cả notifications của user
     */
    fun findByReceiverIdOrderByCreatedAtDesc(receiverId: Long, pageable: Pageable): Page<Notification>
    
    /**
     * Lấy unread notifications của user
     */
    fun findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(receiverId: Long, pageable: Pageable): Page<Notification>
    
    /**
     * Đếm số unread notifications
     */
    fun countByReceiverIdAndIsReadFalse(receiverId: Long): Long
    
    /**
     * Đánh dấu notification là đã đọc
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId AND n.receiver.id = :receiverId")
    fun markAsRead(@Param("notificationId") notificationId: Long, @Param("receiverId") receiverId: Long)
    
    /**
     * Đánh dấu tất cả notifications là đã đọc
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    fun markAllAsRead(@Param("receiverId") receiverId: Long)
    
    /**
     * Xóa notifications cũ (sau 30 ngày)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    fun deleteOldNotifications(@Param("date") date: java.time.LocalDateTime)
}
