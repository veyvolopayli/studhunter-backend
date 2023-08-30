package com.studhunter.api.chat.tables

import com.studhunter.api.chat.model.Chat
import org.jetbrains.exposed.sql.*
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

    /*fun closeChat(chatID: String): Boolean? {
        return try {
            transaction {
                update( { chatId.eq(chatID) } ) {
                    it[active] = false
                } > 0
            }
        } catch (e: Exception) {
            null
        }
    }*/
}