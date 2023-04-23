package com.example.data.responses

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    var rating: Double = 5.0,
    val fullName: String?,
    val email: String?
)
