package com.androidinsta.Model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "user_roles")
@IdClass(UserRoleId::class)
data class UserRole(
    @Id
    @Column(name = "user_id")
    val userId: Long,

    @Id
    @Column(name = "role_id")
    val roleId: Short,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    val role: Role? = null
)

data class UserRoleId(
    val userId: Long = 0,
    val roleId: Short = 0
) : Serializable


