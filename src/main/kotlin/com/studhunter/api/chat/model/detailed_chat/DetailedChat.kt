package com.studhunter.api.chat.model.detailed_chat

import kotlinx.serialization.Serializable

@Serializable
data class DetailedChat(
    val chatId: String,
    val sellerId: String,
    val avatar: String = "https://storage.yandexcloud.net/stud-hunter-bucket/users/avatars/${ sellerId }",
    val lastMessage: String,
    val publicationTitle: String,
    val timestamp: Long
)