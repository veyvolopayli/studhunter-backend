package com.studhunter.api.chat.model

import io.ktor.websocket.*

data class Connection(
    val userId: String,
    val session: WebSocketSession
)