package com.studhunter.api.chat.model

import io.ktor.websocket.*

data class Connection(
    val userID: String,
    val session: WebSocketSession
)