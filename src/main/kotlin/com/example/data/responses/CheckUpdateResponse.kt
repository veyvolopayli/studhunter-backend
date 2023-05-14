package com.example.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class CheckUpdateResponse(
    val exists: Boolean
)