package com.studhunter.api.reviews.requests

data class OpenReviewRequest(
    val userId: String,
    val publicationId: String
)
