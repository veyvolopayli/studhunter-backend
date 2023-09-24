package com.studhunter.api.chat.model

import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@SerialName("Task")
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val executorId: String,
    val customerId: String,
    val publicationId: String,
    val chatId: String,
    val status: String,
    val timestamp: Long
): DataTransfer