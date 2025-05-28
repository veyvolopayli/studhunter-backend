package com.studhunter.api.application.tables

import com.studhunter.api.application.model.Application
import com.studhunter.api.application.model.ApplicationStatus
import com.studhunter.api.application.model.CreateApplicationRequest
import com.studhunter.api.application.repository.ApplicationRepository
import com.studhunter.api.users.tables.Users
import com.studhunter.api.vacancy.tables.Vacancies
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Applications : Table("applications"), ApplicationRepository {
    private val id = uuid("id").autoGenerate()
    private val vacancyId = reference("vacancy_id", Vacancies.id)
    private val studentId = reference("student_id", Users.id)
    private val message = text("message").nullable()
    private val status = enumerationByName("status", 20, ApplicationStatus::class).default(ApplicationStatus.PENDING)
    private val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)

    override fun insert(req: CreateApplicationRequest, studentId: UUID): UUID? = try {
        val newId = UUID.randomUUID()
        transaction {
            insert {
                it[id] = newId
                it[vacancyId] = UUID.fromString(req.vacancyId)
                it[Applications.studentId] = studentId
                it[message] = req.message
                it[status] = ApplicationStatus.PENDING
                it[createdAt] = System.currentTimeMillis()
            }
        }
        newId
    } catch (e: Exception) {
        null
    }

    override fun get(id: String): Application? = transaction {
        select { Applications.id eq UUID.fromString(id) }
            .singleOrNull()?.toApplication()
    }

    override fun getByVacancy(vacancyId: String): List<Application> = transaction {
        select { Applications.vacancyId eq UUID.fromString(vacancyId) }
            .map { it.toApplication() }
    }

    override fun getByStudent(studentId: String): List<Application> = transaction {
        select { Applications.studentId eq UUID.fromString(studentId) }
            .map { it.toApplication() }
    }

    override fun updateStatus(applicationId: String, status: ApplicationStatus): Boolean = transaction {
        update({ Applications.id eq UUID.fromString(applicationId) }) {
            it[Applications.status] = status
        } > 0
    }

    private fun ResultRow.toApplication() = Application(
        id = this[id],
        vacancyId = this[vacancyId],
        studentId = this[studentId],
        message = this[message],
        status = this[status],
        createdAt = this[createdAt]
    )
}
