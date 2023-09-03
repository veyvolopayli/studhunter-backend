package com.studhunter.api.chat.model

import com.studhunter.api.common.convertHoursToMillis
import io.ktor.util.date.*

data class OfferRequestDTO(
//    val chatID: String,
    val jobDeadline: Long
) {
    fun toOfferRequest(chatID: String): OfferRequest {
        return OfferRequest(
            chatID = chatID,
            jobDeadline = this.jobDeadline
        )
    }
}