package com.studhunter.api.chat.model

import com.studhunter.api.common.convertHoursToMillis
import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "OutgoingDealRequest")
data class OfferRequestDTO(
    val jobDeadline: Long
) : DataTransfer {
    fun toOfferRequest(chatID: String): OfferRequest {
        return OfferRequest(
            chatID = chatID,
            timestamp = 1,
            expiredIn = 1,
            jobDeadline = this.jobDeadline
        )
    }
}