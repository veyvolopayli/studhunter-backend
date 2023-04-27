package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class FavoritesPubRequest(
    val userId: String
)
