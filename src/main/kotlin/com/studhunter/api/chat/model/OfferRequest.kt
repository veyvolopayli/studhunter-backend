package com.studhunter.api.chat.model

import com.studhunter.api.common.convertHoursToMillis
import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@SerialName("DealRequest")
data class OfferRequest(
    val id: String = UUID.randomUUID().toString(),
    val chatID: String,
    val timestamp: Long = getTimeMillis(),
    val expiredIn: Long = getTimeMillis(),
    val jobDeadline: Long
) : DataTransfer