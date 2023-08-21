package com.studhunter.api.reviews.model

import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Review(
    val id: String = UUID.randomUUID().toString(),
    val reviewerId: String,
    val userId: String,
    val review: Double?,
    val reviewMessage: String?,
    val timestamp: Long = getTimeMillis(),
    val publicationId: String
)