package com.studhunter.api.reviews.requests

import kotlinx.serialization.Serializable

@Serializable
data class NewReviewRequest(
    val taskId: String,
    val reviewValue: Double,
    val reviewMessage: String = ""
)