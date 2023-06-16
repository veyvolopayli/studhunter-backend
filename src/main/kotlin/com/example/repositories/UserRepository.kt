package com.example.repositories

import com.example.data.models.User
import com.example.data.responses.UserResponse

interface UserRepository {
    fun getUserByUsername(username: String): UserResponse?
    fun getUserByEmail(email: String): UserResponse?
    fun getUserById(id: String): UserResponse?
    fun insertUser(user: User): String?
    fun getUserDetailed(username: String): User?
}