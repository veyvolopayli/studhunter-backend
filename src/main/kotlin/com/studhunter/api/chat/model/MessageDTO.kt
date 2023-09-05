package com.studhunter.api.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("OutgoingMessage")
data class MessageDTO(
    val messageBody: String,
    val messageType: String
) : DataTransfer

fun MessageDTO.toMessage(chatID: String, fromID: String): Message {
    return Message(
        fromId = fromID,
        messageBody = this.messageBody,
        messageType = this.messageType,
        chatId = chatID
    )
}
