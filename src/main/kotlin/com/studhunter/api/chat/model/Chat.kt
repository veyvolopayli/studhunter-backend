package com.studhunter.api.chat.model

import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import java.util.*
@Serializable
data class Chat(
    val id: String = UUID.randomUUID().toString(),
    val publicationId: String,
    val customerId: String,
    val sellerId: String,
    var lastMessage: String,
    val timestamp: Long = getTimeMillis()
)
