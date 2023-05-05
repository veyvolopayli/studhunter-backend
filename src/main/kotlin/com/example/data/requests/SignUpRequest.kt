package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val username: String,
    val password: String,
    val email: String,
    val name: String,
    val surname: String? = null,
    val university: String? = null
)