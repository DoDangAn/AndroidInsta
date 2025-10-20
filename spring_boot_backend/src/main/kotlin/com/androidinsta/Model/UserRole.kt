package com.androidinsta.Model

import jakarta.persistence.*

@Entity
@Table(name = "user_roles")
@IdClass(UserRoleId::class)
data class UserRole(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    val role: Role
)

// Composite key class
data class UserRoleId(
    val user: Long = 0,
    val role: Short = 0
) : java.io.Serializable