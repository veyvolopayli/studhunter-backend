package com.studhunter.api.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("OutgoingDealResponse")
data class OfferResponseDTO(
    val accepted: Boolean
) : DataTransfer {
    fun toOfferResponse(chatID: String, requestID: String): OfferResponse {
        return OfferResponse(
            positive = this.accepted,
            chatID = chatID,
            requestID = requestID
        )
    }
}