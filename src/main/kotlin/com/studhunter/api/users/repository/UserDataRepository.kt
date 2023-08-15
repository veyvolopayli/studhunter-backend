package com.studhunter.api.users.repository

import com.studhunter.api.users.model.UserDataModel

interface UserDataRepository {
    fun insertUserData(userDataModel: UserDataModel): Int?
    fun fetchUserEmailConfirmed(userId: String): Boolean?
    fun confirmEmail(userId: String, code: Int): Boolean?
    fun updateConfirmationCode(userId: String): Int?
}