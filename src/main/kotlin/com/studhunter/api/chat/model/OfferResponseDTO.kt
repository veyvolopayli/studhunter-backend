package com.studhunter.api.chat.model

data class OfferResponseDTO(
    val accepted: Boolean
) {
    fun toOfferResponse(chatID: String, requestID: String): OfferResponse {
        return OfferResponse(
            accepted = this.accepted,
            chatID = chatID,
            requestID = requestID
        )
    }
}