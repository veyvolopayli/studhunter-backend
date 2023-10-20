package com.studhunter.api.chat.model.detailed_chat

import com.studhunter.api.chat.model.Chat

data class DetailedChat(
    val chat: Chat,
    val avatar: String = "https://storage.yandexcloud.net/stud-hunter-bucket/users/avatars/${ chat.sellerId }",
    val lastMessage: LastMessage,
    val publicationTitle: String
)