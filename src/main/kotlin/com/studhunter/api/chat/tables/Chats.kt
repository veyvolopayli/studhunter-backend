package com.studhunter.api.chat.tables

import com.studhunter.api.chat.model.Chat
import com.studhunter.api.chat.model.detailed_chat.DetailedChat
import com.studhunter.api.publications.tables.Publications
import com.studhunter.api.vacancy.tables.Vacancies
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Chats : Table() {
    private val id = varchar("chat_id", 36)
    private val vacancyId = uuid("vacancy_id").index()
    private val employerId = uuid("employer_id").index()
    private val studentId = uuid("student_id").index()
    private val lastMessage = text("last_message").default("")
    private val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)

    private fun ResultRow.toChat() = Chat(
        id = this[id],
        vacancyId = this[vacancyId].toString(),
        employerId = this[employerId].toString(),
        studentId = this[studentId].toString(),
        lastMessage = this[lastMessage],
        createdAt = this[createdAt]
    )

    fun get(chatId: String): Chat? = transaction {
        select { Chats.id eq chatId }.singleOrNull()?.toChat()
    }

    fun getUserChats(userId: String): List<Chat> = transaction {
        select { (employerId eq UUID.fromString(userId)) or (studentId eq UUID.fromString(userId)) }
            .orderBy(createdAt, SortOrder.DESC)
            .map { it.toChat() }
    }

    fun getOrCreate(studentId: String, vacancyId: String): Chat? = try {
        transaction {
            val existing = select {
                (Chats.studentId eq UUID.fromString(studentId)) and
                        (Chats.vacancyId eq UUID.fromString(vacancyId))
            }.singleOrNull()
            if (existing != null) return@transaction existing.toChat()

            val vacancy = Vacancies.getVacancy(vacancyId) ?: return@transaction null
            val newChat = Chat(
                vacancyId = vacancyId,
                employerId = vacancy.employerId.toString(),
                studentId = studentId
            )
            insert {
                it[id] = newChat.id
                it[Chats.vacancyId] = UUID.fromString(newChat.vacancyId)
                it[Chats.employerId] = UUID.fromString(newChat.employerId)
                it[Chats.studentId] = UUID.fromString(newChat.studentId)
                it[lastMessage] = newChat.lastMessage
                it[createdAt] = newChat.createdAt
            }
            newChat
        }
    } catch (_: Exception) { null }

    fun updateLastMessage(chatId: String, text: String) = transaction {
        update({ Chats.id eq chatId }) { it[lastMessage] = text }
    }
}