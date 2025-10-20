package com.androidinsta

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AndroidInstaApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<AndroidInstaApplication>(*args)
        }
    }
}