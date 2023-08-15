package com.studhunter.api.users.responses

import kotlinx.serialization.Serializable

@Serializable
data class ShortUserResponse(
    val id: String,
    val username: String,
    val university: String?
)
