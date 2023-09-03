package com.studhunter.api.chat.model

import com.studhunter.api.common.convertHoursToMillis
import io.ktor.util.date.*
import java.util.UUID

data class OfferRequest(
    val id: String = UUID.randomUUID().toString(),
    val chatID: String,
    val timestamp: Long = getTimeMillis(),
    val expiredIn: Long = timestamp + convertHoursToMillis(1),
    val jobDeadline: Long
)