package com.studhunter.api.chat.model

import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("Message")
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val fromId: String,
    val timestamp: Long = getTimeMillis(),
    val messageBody: String,
    val chatId: String,
    val messageType: String
) : DataTransfer