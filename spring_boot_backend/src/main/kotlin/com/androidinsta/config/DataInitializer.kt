package com.androidinsta.config

import com.androidinsta.Model.Role
import com.androidinsta.Repository.User.RoleRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val roleRepository: RoleRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        initializeRoles()
    }

    private fun initializeRoles() {
        if (roleRepository.count() == 0L) {
            val userRole = Role(
                name = "USER",
                description = "Normal user role"
            )
            
            val adminRole = Role(
                name = "ADMIN", 
                description = "Administrator role"
            )

            roleRepository.save(userRole)
            roleRepository.save(adminRole)
            
            println("✅ Roles initialized: USER, ADMIN")
        } else {
            println("✅ Roles already exist in database")
        }
    }
}