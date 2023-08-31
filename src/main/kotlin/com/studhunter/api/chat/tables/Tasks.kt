package com.studhunter.api.chat.tables

import com.studhunter.api.chat.model.Task
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object Tasks : Table() {
    private val ID = varchar("id", 36)
    private val executorID = varchar("executor_id", 36)
    private val customerID = varchar("customer_id", 36)
    private val pubID = varchar("publication_id", 36)
    private val chatID = varchar("chat_id", 36)
    private val timestamp = long("timestamp")
    private val deadlineTimestamp = long("deadline_timestamp")

    fun insertTask(task: Task): Boolean? {
        return try {
            transaction {
                insert {
                    it[ID] = task.ID
                    it[executorID] = task.executorID
                    it[customerID] = task.customerID
                    it[pubID] = task.publicationID
                    it[chatID] = task.chatID
                    it[timestamp] = task.timestamp
                    it[deadlineTimestamp] = task.deadlineTimestamp
                }.insertedCount > 0
            }
        } catch (e: Exception) {
            null
        }
    }
}