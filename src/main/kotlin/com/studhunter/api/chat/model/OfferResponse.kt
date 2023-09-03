package com.studhunter.api.chat.model

import io.ktor.util.date.*
import java.util.UUID

data class OfferResponse(
    val id: String = UUID.randomUUID().toString(),
    val chatID: String,
    val timestamp: Long = getTimeMillis(),
    val accepted: Boolean,
    val requestID: String
)