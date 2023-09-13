package com.studhunter.api.chat.tables

import com.studhunter.api.chat.repository.UserChatRepository
import com.studhunter.api.chat.model.Message
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object UserChatMessages : Table("user_chat_messages"), UserChatRepository {
    private val messageId = varchar("id", 36)
    private val fromId = varchar("from_id", 36)
    private val timestamp = long("timestamp")
    private val messageBody = varchar("message_body", 150)
    private val chatId = varchar("chat_id", 36)
    private val type = varchar("type", 20)

    override fun insertMessage(message: Message): String? {
        return try {
            transaction {
                insert {
                    it[messageId] = message.id
                    it[fromId] = message.fromId
                    it[timestamp] = message.timestamp
                    it[messageBody] = message.messageBody
                    it[chatId] = message.chatId
                    it[type] = message.messageType
                }
            }
            message.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getMessages(chatId: String): List<Message>? {
        return try {
            transaction {
                select { UserChatMessages.chatId eq chatId }.map { row ->
                    Message(
                        id = row[messageId],
                        fromId = row[fromId],
                        timestamp = row[timestamp],
                        messageBody = row[messageBody],
                        chatId = row[UserChatMessages.chatId],
                        messageType = row[type]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getMessages(userId: String, publicationId: String): List<Message>? {
        return try {
            val chat = Chats.fetchChat(userID = userId, publicationID = publicationId) ?: return null
            val messages = getMessages(chat.id) ?: emptyList()
            messages
        } catch (e: Exception) {
            null
        }
    }

    override fun deleteChat(chatId: String): Int? {
        return try {
            transaction {
                deleteWhere { UserChatMessages.chatId eq chatId }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun archiveChat(chatId: String): Boolean? {
        return null
    }

}