package com.studhunter.api.updates.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRequest(
    val version: String
)
