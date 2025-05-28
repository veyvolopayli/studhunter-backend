package com.studhunter.api.employer.tables

import com.studhunter.api.employer.dto.CreateEmployerProfileRequest
import com.studhunter.api.employer.dto.UpdateEmployerProfileRequest
import com.studhunter.api.employer.model.EmployerProfile
import com.studhunter.api.employer.repository.EmployerRepository
import com.studhunter.api.users.tables.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Employers : Table("employers"), EmployerRepository {
    private val id = uuid("id").autoGenerate()
    private val userId = reference("user_id", Users.id).uniqueIndex()
    private val name = varchar("name", 150)
    private  val description = text("description").nullable()
    private val website = varchar("website", 255).nullable()
    private val logoUrl = varchar("logo_url", 255).nullable()

    override val primaryKey = PrimaryKey(id)

    override fun insertProfile(userId: UUID, req: CreateEmployerProfileRequest): UUID? = try {
        val newId = UUID.randomUUID()
        transaction {
            insert {
                it[id] = newId
                it[Employers.userId] = userId
                it[name] = req.name
                it[description] = req.description
                it[website] = req.website
                it[logoUrl] = req.logoUrl
            }
        }
        newId
    } catch (e: Exception) { null }

    override fun getProfile(userId: String): EmployerProfile? = try {
        transaction {
            select { Employers.userId eq UUID.fromString(userId) }
                .singleOrNull()
                ?.let {
                    EmployerProfile(
                        id = it[Employers.id],
                        userId = it[Employers.userId],
                        name = it[name],
                        description = it[description],
                        website = it[website],
                        logoUrl = it[logoUrl]
                    )
                }
        }
    } catch (e: Exception) { null }

    override fun updateProfile(userId: String, req: UpdateEmployerProfileRequest): Boolean? = try {
        transaction {
            update({ Employers.userId eq UUID.fromString(userId) }) {
                req.name?.let { n -> it[name] = n }
                req.description?.let { d -> it[description] = d }
                req.website?.let { w -> it[website] = w }
                req.logoUrl?.let { l -> it[logoUrl] = l }
            } > 0
        }
    } catch (e: Exception) { null }

    override fun deleteProfile(userId: String): Boolean? = try {
        transaction {
            deleteWhere { Employers.userId eq UUID.fromString(userId) } > 0
        }
    } catch (e: Exception) { null }
}
