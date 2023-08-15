package com.studhunter.api.reviews.requests

import kotlinx.serialization.Serializable

@Serializable
data class InsertReviewRequest(
    val id: String,
    val review: Double,
    val reviewMessage: String?,
)
