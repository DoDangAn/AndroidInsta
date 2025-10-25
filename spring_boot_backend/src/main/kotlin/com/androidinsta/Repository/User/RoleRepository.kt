package com.androidinsta.Repository.User

import com.androidinsta.Model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RoleRepository : JpaRepository<Role, Short> {
    fun findByName(name: String): Optional<Role>
}