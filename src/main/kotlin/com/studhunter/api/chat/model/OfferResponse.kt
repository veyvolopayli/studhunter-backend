package com.studhunter.api.chat.model

import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
@Serializable
@SerialName("DealResponse")
data class OfferResponse(
    val id: String = UUID.randomUUID().toString(),
    val chatID: String,
    val timestamp: Long = getTimeMillis(),
    val positive: Boolean,
    val requestID: String
) : DataTransfer