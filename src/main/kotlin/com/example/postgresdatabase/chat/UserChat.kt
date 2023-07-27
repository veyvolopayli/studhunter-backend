package com.example.postgresdatabase.chat

import com.example.chat.UserChatService
import com.example.chat.model.Message
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object UserChat : Table(), UserChatService {
    private val messageId = varchar("id", 36)
    private val authorId = varchar("authorId", 36)
    private val subjectId = varchar("subjectId", 36)
    private val timestamp = long("timestamp")
    private val messageBody = varchar("messageBody", 150)
    private val chatId = varchar("chatId", 36)
    private val publicationId = varchar("publicationId", 36)
    private val type = varchar("type", 20)

    override fun insertMessage(message: Message): String? {
        return try {
            transaction {
                insert {
                    it[messageId] = message.id
                    it[authorId] = message.authorId
                    it[subjectId] = message.subjectId
                    it[timestamp] = message.timestamp
                    it[messageBody] = message.messageBody
                    it[chatId] = message.chatId
                    it[publicationId] = message.publicationId
                    it[type] = message.type
                }
            }
            message.id
        } catch (e: Exception) {
            null
        }
    }

    override fun getMessages(chatId: String): List<Message>? {
        return try {
            transaction {
                select { UserChat.chatId eq chatId }.map { row ->
                    Message(
                        id = row[messageId],
                        authorId = row[authorId],
                        subjectId = row[subjectId],
                        timestamp = row[timestamp],
                        messageBody = row[messageBody],
                        chatId = row[UserChat.chatId],
                        publicationId = row[publicationId],
                        type = row[type]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun deleteChat(chatId: String): Int? {
        return try {
            transaction {
                deleteWhere { UserChat.chatId eq chatId }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun archiveChat(chatId: String): Boolean? {
        return null
    }

}