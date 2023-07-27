package com.example.chat.model

import io.ktor.websocket.*

data class ChatMember(
    val id: String,
    val sessionId: String,
    val socket: WebSocketSession
)
