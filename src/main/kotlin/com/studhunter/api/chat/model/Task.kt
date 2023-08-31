package com.studhunter.api.chat.model

import io.ktor.util.date.*
import java.util.UUID

data class Task(
    val ID: String = UUID.randomUUID().toString(),
    val executorID: String,
    val customerID: String,
    val publicationID: String,
    val chatID: String,
    val timestamp: Long = getTimeMillis(),
    val deadlineTimestamp: Long
)