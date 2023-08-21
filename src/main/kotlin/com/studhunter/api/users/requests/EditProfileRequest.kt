package com.studhunter.api.users.requests

import kotlinx.serialization.Serializable

@Serializable
data class EditProfileRequest(
    val name: String,
    val surname: String,
    val university: String
)
