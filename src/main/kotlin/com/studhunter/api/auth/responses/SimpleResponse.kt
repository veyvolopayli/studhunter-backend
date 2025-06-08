package com.studhunter.api.auth.responses

import kotlinx.serialization.Serializable

@Serializable
data class SimpleResponse<T>(
    val data: T
)
