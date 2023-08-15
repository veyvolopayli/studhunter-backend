package com.studhunter.api.users.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    var rating: Double,
    val name: String,
    val surname: String?,
    val email: String,
    val university: String?
)

fun UserResponse.toShortUserResponse(): ShortUserResponse {
    return ShortUserResponse(
        id = id, username = username, university = university
    )
}