package com.studhunter.api.chat.model

data class OfferRequest(
    val id: String,
    val chatID: String,
    val timestamp: Long,
    val expiredIn: Long,
    val jobDeadline: Long,
    val userID: String
)