package com.studhunter.api.chat.model

import kotlinx.serialization.Serializable

@Serializable
data class IncomingTextFrame(
    val type: String,
    val data: DataTransfer
)
