package com.studhunter.api.users.repository

import java.io.File

interface YCloudUsersRepository {
    suspend fun insertUserAvatar(file: File, userID: String): Boolean
}