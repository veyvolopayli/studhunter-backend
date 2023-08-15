package com.studhunter.api.users.model

import io.ktor.util.date.*
import kotlin.random.Random

data class UserDataModel(
    val userId: String,
    val joinDate: Long = getTimeMillis(),
    val emailConfirmed: Boolean = false,
    val confirmationCode: Int = Random.nextInt(333333, 999999)
)
