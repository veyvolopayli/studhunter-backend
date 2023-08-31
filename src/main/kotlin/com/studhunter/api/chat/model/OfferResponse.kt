package com.studhunter.api.chat.model

data class OfferResponse(
    val id: String,
    val chatID: String,
    val timestamp: Long,
    val accepted: Boolean,
    val requestID: String
)