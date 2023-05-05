package com.example.chat.model

data class Message(
    val id: String,
    val creatorId: String,
    val subjectId: String,
    val timestamp: Long,
    val messageBody: String
)