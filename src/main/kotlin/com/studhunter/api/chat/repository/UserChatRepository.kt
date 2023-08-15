package com.studhunter.api.chat.repository

import com.studhunter.api.chat.model.Message

interface UserChatRepository {

    fun insertMessage(message: Message): String?

    fun getMessages(chatId: String): List<Message>?

    fun deleteChat(chatId: String): Int?

    fun archiveChat(chatId: String): Boolean?

}