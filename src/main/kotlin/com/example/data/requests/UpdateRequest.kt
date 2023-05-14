package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRequest(
    val version: String
)
