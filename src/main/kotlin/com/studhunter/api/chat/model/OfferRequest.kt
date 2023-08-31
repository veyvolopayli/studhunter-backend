package com.studhunter.api.chat.model

import com.studhunter.api.common.convertHoursToMillis
import io.ktor.util.date.*

data class OfferRequest(
    val id: String,
    val chatID: String,
    val timestamp: Long = getTimeMillis(),
    val expiredIn: Long = timestamp + convertHoursToMillis(1),
    val jobDeadline: Long
)