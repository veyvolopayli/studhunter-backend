package com.example.data.models

import com.example.features.getCurrentMills
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Review(
    val id: String = UUID.randomUUID().toString(),
    val reviewerId: String,
    val userId: String,
    val review: Double,
    val reviewMessage: String?,
    val timestamp: Long = getCurrentMills(),
    val publicationId: String
)
