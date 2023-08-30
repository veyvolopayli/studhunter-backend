package com.studhunter.api.chat.features

import com.studhunter.api.chat.model.MessageDTO
import com.studhunter.api.chat.model.OfferRequest
import com.studhunter.api.chat.model.OfferResponse
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun getTextFrameType(frame: Frame.Text): TextFrameType {
    val jsonString = frame.readText()
    try {
        val messageDTO = Json.decodeFromString<MessageDTO>(jsonString)
        return TextFrameType.TMessage(messageDTO)
    } catch (_: Exception) { }
    try {
        val offerRequest = Json.decodeFromString<OfferRequest>(jsonString)
        return TextFrameType.TOffer(offerRequest)
    } catch (_: Exception) { }
    try {
        val offerResponse = Json.decodeFromString<OfferResponse>(jsonString)
        return TextFrameType.TOfferResponse(offerResponse)
    } catch (_: Exception) { }
    return TextFrameType.TOther(Unit)
}