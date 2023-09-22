package com.studhunter.api.chat.tables

import com.studhunter.api.chat.model.Task
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object Tasks : Table() {
    private val id = varchar("id", 36)
    private val executorId = varchar("executor_id", 36)
    private val customerId = varchar("customer_id", 36)
    private val pubId = varchar("publication_id", 36)
    private val chatId = varchar("chat_id", 36)
    private val timestamp = long("timestamp")
    private val status = varchar("status", 20)

    fun insertTask(task: Task): Boolean? {
        return try {
            transaction {
                insert {
                    it[id] = task.id
                    it[executorId] = task.executorId
                    it[customerId] = task.customerId
                    it[pubId] = task.publicationId
                    it[chatId] = task.chatId
                    it[timestamp] = task.timestamp
                    it[status] = task.status
                }.insertedCount > 0
            }
        } catch (e: Exception) {
            null
        }
    }
}