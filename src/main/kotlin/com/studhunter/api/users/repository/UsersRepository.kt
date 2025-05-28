package com.studhunter.api.users.repository

import com.studhunter.api.users.model.User
import com.studhunter.api.users.requests.EditProfileRequest
import com.studhunter.api.users.responses.UserResponse
import java.util.UUID

interface UsersRepository {
    fun getUserByUsername(username: String): UserResponse?
    fun getUserByEmail(email: String): UserResponse?
    fun getUserById(id: String): UserResponse?
    fun insertUser(user: User): UUID?
    fun getUserDetailed(username: String): User?
    fun editUser(userID: String, editProfileRequest: EditProfileRequest): Boolean?
}