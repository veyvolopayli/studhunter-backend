package com.example.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserStatusResponse(
    val emailConfirmed: Boolean
)
