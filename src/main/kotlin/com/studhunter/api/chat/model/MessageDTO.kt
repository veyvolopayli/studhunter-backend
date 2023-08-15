package com.studhunter.api.chat.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageDTO(
    val messageBody: String,
    val type: String
)

fun MessageDTO.toMessage(chatID: String, fromID: String): Message {
    return Message(
        fromId = fromID,
        messageBody = this.messageBody,
        type = this.type,
        chatId = chatID
    )
}
