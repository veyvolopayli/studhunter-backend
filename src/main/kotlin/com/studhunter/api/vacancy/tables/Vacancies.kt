package com.studhunter.api.vacancy.tables

import com.studhunter.api.users.tables.Users
import com.studhunter.api.vacancy.dto.CreateVacancyRequest
import com.studhunter.api.vacancy.dto.UpdateVacancyRequest
import com.studhunter.api.vacancy.model.Vacancy
import com.studhunter.api.vacancy.model.VacancyFilter
import com.studhunter.api.vacancy.repository.VacancyRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Vacancies : Table("vacancies"), VacancyRepository {
    internal val id = uuid("id").autoGenerate()
    private val employerId = reference("employer_id", Users.id).index()
    private val title = varchar("title", 120)
    private val description = text("description")
    private val category = varchar("category", 60)
    private val format = varchar("format", 20).nullable()
    private val region = varchar("region", 60).nullable()
    private val schedule = varchar("schedule", 40).nullable()
    private val payment = varchar("payment", 60).nullable()
    private val createdAt = long("created_at")
    private val closesAt = long("closes_at").nullable()
    private val active = bool("active").default(true)

    override val primaryKey  = PrimaryKey(id)

    override fun insertVacancy(employerId: UUID, req: CreateVacancyRequest): UUID? = try {
        val newId = UUID.randomUUID()
        transaction {
            insert {
                it[id] = newId
                it[Vacancies.employerId] = employerId
                it[title] = req.title
                it[description] = req.description
                it[category] = req.category
                it[format] = req.format
                it[region] = req.region
                it[schedule] = req.schedule
                it[payment] = req.payment
                it[createdAt] = System.currentTimeMillis()
                it[closesAt] = req.closesAt
            }
        }
        newId
    } catch (e: Exception) { null }

    override fun getVacancy(id: String): Vacancy? = try {
        transaction {
            select { Vacancies.id eq UUID.fromString(id) }
                .singleOrNull()
                ?.toVacancy()
        }
    } catch (e: Exception) { null }

    override fun updateVacancy(id: String, req: UpdateVacancyRequest): Boolean? = try {
        transaction {
            update({ Vacancies.id eq UUID.fromString(id) }) {
                req.title?.let { t -> it[title] = t }
                req.description?.let { d -> it[description] = d }
                req.category?.let { c -> it[category] = c }
                req.format?.let { f -> it[format] = f }
                req.region?.let { r -> it[region] = r }
                req.schedule?.let { s -> it[schedule] = s }
                req.payment?.let { p -> it[payment] = p }
                req.closesAt?.let { c -> it[closesAt] = c }
                req.active?.let { a -> it[active] = a }
            } > 0
        }
    } catch (e: Exception) { null }

    override fun deleteVacancy(id: String, employerId: String): Boolean? = try {
        transaction {
            deleteWhere {
                (Vacancies.id eq UUID.fromString(id)) and
                (Vacancies.employerId eq UUID.fromString(employerId))
            } > 0
        }
    } catch (e: Exception) { null }

    override fun search(filter: VacancyFilter): List<Vacancy>? = try {
        transaction {
            select {
                (filter.category?.let { category eq it } ?: Op.TRUE) and
                (filter.region?.let { region eq it } ?: Op.TRUE) and
                (filter.format?.let { format eq it } ?: Op.TRUE) and
                (active eq true)
            }
                .orderBy(createdAt, SortOrder.DESC)
                .map { it.toVacancy() }
        }
    } catch (e: Exception) { null }

    private fun ResultRow.toVacancy() = Vacancy(
        id = this[id],
        employerId = this[employerId],
        title = this[title],
        description = this[description],
        category = this[category],
        format = this[format],
        region = this[region],
        schedule = this[schedule],
        payment = this[payment],
        createdAt = this[createdAt],
        closesAt = this[closesAt],
        active = this[active]
    )
}