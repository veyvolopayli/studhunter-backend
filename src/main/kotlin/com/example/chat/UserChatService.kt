package com.example.chat

import com.example.chat.model.Message

interface UserChatService {

    fun insertMessage(message: Message): String?

    fun getMessages(chatId: String): List<Message>?

    fun deleteChat(chatId: String): Int?

    fun archiveChat(chatId: String): Boolean?

}