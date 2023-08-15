package com.studhunter.api.auth.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)
