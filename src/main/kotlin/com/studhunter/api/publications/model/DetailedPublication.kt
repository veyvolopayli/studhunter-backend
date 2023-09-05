package com.studhunter.api.publications.model

import com.studhunter.api.users.responses.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class DetailedPublication(
    val publication: Publication,
    val user: UserResponse,
    val userIsOwner: Boolean
)