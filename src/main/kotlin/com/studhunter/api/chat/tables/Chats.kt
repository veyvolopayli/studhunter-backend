package com.studhunter.api.chat.tables

import com.studhunter.api.chat.model.Chat
import com.studhunter.api.chat.model.detailed_chat.DetailedChat
import com.studhunter.api.chat.model.detailed_chat.LastMessage
import com.studhunter.api.publications.tables.Publications
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Chats : Table() {
    private val chatId = varchar("chat_id", 36)
    private val publicationId = varchar("publication_id", 36)
    private val customerId = varchar("customer_id", 36)
    private val sellerId = varchar("seller_id", 36)
    private val lastMessage = varchar("last_message", 36)
    private val timestamp = long("timestamp")

    fun insertChat(chat: Chat): String? {
        return try {
            transaction {
                insert {
                    it[chatId] = chat.id
                    it[publicationId] = chat.publicationId
                    it[customerId] = chat.customerId
                    it[sellerId] = chat.sellerId
                    it[lastMessage] = chat.lastMessage
                    it[timestamp] = chat.timestamp
                }
            }
            chat.id
        } catch (e: Exception) {
            null
        }
    }

    fun fetchChats(userId: String): List<Chat>? {
        return try {
            transaction {
                select { customerId.eq(userId) or sellerId.eq(userId) }.map {
                    Chat(
                        id = it[chatId],
                        publicationId = it[publicationId],
                        sellerId = it[sellerId],
                        customerId = it[customerId],
                        lastMessage = it[lastMessage],
                        timestamp = it[timestamp]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchChatsDetailed(userId: String): List<DetailedChat>? {
        return try {
            transaction {
                Chats.join(UserChatMessages, JoinType.INNER, onColumn = chatId, otherColumn = UserChatMessages.chatId)
                    .join(Publications, JoinType.INNER, onColumn = publicationId, otherColumn = Publications.id)
                    .slice(chatId, sellerId, UserChatMessages.messageBody, Publications.title, timestamp)
                    .select { sellerId.eq(userId) or customerId.eq(userId) }
                    .map { row ->
                        DetailedChat(
                            chatId = row[chatId],
                            sellerId = row[sellerId],
                            lastMessage = row[UserChatMessages.messageBody],
                            publicationTitle = row[Publications.title],
                            timestamp = row[timestamp]
                        )
                    }.groupBy { it.chatId }.values
                    .map { it.last() }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fetchChat(chatID: String): Chat? {
        return try {
            transaction {
                val row = select { chatId eq chatID }.first()
                Chat(
                    id = row[chatId],
                    publicationId = row[publicationId],
                    sellerId = row[sellerId],
                    customerId = row[customerId],
                    lastMessage = row[lastMessage],
                    timestamp = row[timestamp]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchChat(publicationID: String, userID: String): Chat? {
        return try {
            transaction {
                val row = select { publicationId.eq(publicationID) and customerId.eq(userID) }.first()
                Chat(
                    id = row[chatId],
                    publicationId = row[publicationId],
                    sellerId = row[sellerId],
                    customerId = row[customerId],
                    lastMessage = row[lastMessage],
                    timestamp = row[timestamp]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getChatOrCreate(userId: String, publicationId: String): Chat? {
        try {
            return transaction {
                val rows = select { customerId.eq(userId) and Chats.publicationId.eq(publicationId) }
                if (rows.count().toInt() == 1) {
                    val row = rows.single()
                    return@transaction Chat(
                        id = row[chatId],
                        publicationId = row[Chats.publicationId],
                        sellerId = row[sellerId],
                        customerId = row[customerId],
                        lastMessage = row[lastMessage],
                        timestamp = row[timestamp]
                    )
                } else if (rows.count() < 1) {
                    val publication = Publications.getPublication(publicationId) ?: return@transaction null

                    val chat = Chat(
                        publicationId = publicationId,
                        sellerId = publication.userId,
                        customerId = userId,
                        lastMessage = ""
                    )
                    insert {
                        it[chatId] = chat.id
                        it[Chats.publicationId] = chat.publicationId
                        it[sellerId] = chat.sellerId
                        it[customerId] = chat.customerId
                        it[lastMessage] = chat.lastMessage
                        it[timestamp] = chat.timestamp
                    }
                    return@transaction chat
                } else {
                    deleteWhere { customerId.eq(userId) and Chats.publicationId.eq(publicationId) }
                    return@transaction null
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun niggers() {

    }
}