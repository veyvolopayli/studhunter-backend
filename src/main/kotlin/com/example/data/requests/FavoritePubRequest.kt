package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class FavoritePubRequest(
    val publicationId: String
)
