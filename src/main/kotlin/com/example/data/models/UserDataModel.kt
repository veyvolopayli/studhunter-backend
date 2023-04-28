package com.example.data.models

import com.example.features.getCurrentMills
import kotlin.random.Random

data class UserDataModel(
    val userId: String,
    val joinDate: Long = getCurrentMills(),
    val emailConfirmed: Boolean = false,
    val confirmationCode: Int = Random.nextInt(333333, 999999)
)
