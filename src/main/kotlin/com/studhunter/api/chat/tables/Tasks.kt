package com.studhunter.api.chat.tables

import com.studhunter.api.chat.model.Task
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Tasks : Table() {
    private val id = varchar("id", 36)
    private val executorId = varchar("executor_id", 36)
    private val customerId = varchar("customer_id", 36)
    private val pubId = varchar("publication_id", 36)
    private val chatId = varchar("chat_id", 36)
    private val timestamp = long("timestamp")
    private val status = varchar("status", 20)
    private val deadline = long("deadline")

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
                    it[deadline] = task.deadline
                }.insertedCount > 0
            }
        } catch (e: Exception) {
            null
        }
    }

    fun updateTask(task: Task): Boolean? {
        return try {
            transaction {
                update({ Tasks.id.eq(task.id) }) {
                    it[id] = task.id
                    it[executorId] = task.executorId
                    it[customerId] = task.customerId
                    it[pubId] = task.publicationId
                    it[chatId] = task.chatId
                    it[timestamp] = task.timestamp
                    it[status] = task.status
                    it[deadline] = task.deadline
                } > 0
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getTask(chatId: String): Task? {
        return try {
            transaction {
                val row = select { Tasks.chatId.eq(chatId) }.single()
                Task(
                    id = row[Tasks.id],
                    executorId = row[executorId],
                    customerId = row[customerId],
                    publicationId = row[pubId],
                    chatId = row[Tasks.chatId],
                    timestamp = row[timestamp],
                    status = row[status],
                    deadline = row[deadline]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getTasks(userId: String, userStatus: String, taskStatus: String): List<Task>? {
        return try {
            val userIdColumn = when (userStatus) {
                "executor" -> executorId
                "customer" -> customerId
                else -> return null
            }
            if (taskStatus !in listOf("accepted", "declined", "complete")) return null
            transaction {
                select { userIdColumn.eq(userId) and status.eq(taskStatus) }.map {
                    Task(
                        id = it[Tasks.id],
                        executorId = it[executorId],
                        customerId = it[customerId],
                        chatId = it[chatId],
                        publicationId = it[pubId],
                        status = it[status],
                        timestamp = it[timestamp],
                        deadline = it[deadline]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getTasks(userId: String, userStatus: String): List<Task>? {
        return try {
            val userIdColumn = when (userStatus) {
                "executor" -> executorId
                "customer" -> customerId
                else -> return null
            }
            transaction {
                select { userIdColumn.eq(userId) }.map {
                    Task(
                        id = it[Tasks.id],
                        executorId = it[executorId],
                        customerId = it[customerId],
                        chatId = it[chatId],
                        publicationId = it[pubId],
                        status = it[status],
                        timestamp = it[timestamp],
                        deadline = it[deadline]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}