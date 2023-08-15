package com.studhunter.api.users.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserStatusResponse(
    val emailConfirmed: Boolean
)
