package com.studhunter.api.updates.responses

import kotlinx.serialization.Serializable

@Serializable
data class CheckUpdateResponse(
    val exists: Boolean
)