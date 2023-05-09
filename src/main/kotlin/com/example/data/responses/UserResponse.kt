package com.example.data.responses

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    var rating: Double,
    val name: String?,
    val surname: String?,
    val email: String,
    val university: String?
)
