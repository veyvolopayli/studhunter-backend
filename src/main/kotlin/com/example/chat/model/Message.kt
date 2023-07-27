package com.example.chat.model

import com.example.features.getCurrentMills
import io.ktor.util.date.*
import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val subjectId: String,
    val timestamp: Long = getTimeMillis(),
    val messageBody: String,
    val chatId: String,
    val publicationId: String,
    val type: String
)