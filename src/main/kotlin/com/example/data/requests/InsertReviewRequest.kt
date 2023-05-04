package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class InsertReviewRequest(
    val userId: String,
    val review: Double,
    val reviewMessage: String?,
    val publicationId: String
)
