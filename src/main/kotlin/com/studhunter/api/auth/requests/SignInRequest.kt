package com.studhunter.api.auth.requests

import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    val username: String,
    val password: String
)
