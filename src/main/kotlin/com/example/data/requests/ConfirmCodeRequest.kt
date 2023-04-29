package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmCodeRequest(
    val userId: String,
    val code: Int
)
