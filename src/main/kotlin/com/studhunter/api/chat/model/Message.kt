package com.studhunter.api.chat.model

import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val fromId: String,
    val timestamp: Long = getTimeMillis(),
    val messageBody: String,
    val chatId: String,
    val type: String
)