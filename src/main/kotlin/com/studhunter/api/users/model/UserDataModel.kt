package com.studhunter.api.users.model

import io.ktor.util.date.*
import java.util.*
import kotlin.random.Random

data class UserDataModel(
    val userId: UUID,
    val joinDate: Long = getTimeMillis(),
    val emailConfirmed: Boolean = false,
    val confirmationCode: Int = Random.nextInt(333333, 999999)
)
