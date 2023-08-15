package com.studhunter.api.users.repository

import com.studhunter.api.users.model.User
import com.studhunter.api.users.responses.UserResponse

interface UsersRepository {
    fun getUserByUsername(username: String): UserResponse?
    fun getUserByEmail(email: String): UserResponse?
    fun getUserById(id: String): UserResponse?
    fun insertUser(user: User): String?
    fun getUserDetailed(username: String): User?
}