package com.studhunter.api.publications.requests

import kotlinx.serialization.Serializable

@Serializable
data class FavoritePubRequest(
    val publicationId: String
)
