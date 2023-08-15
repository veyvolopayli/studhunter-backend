package com.studhunter.api.email.requests

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmCodeRequest(
    val userId: String,
    val code: Int
)
