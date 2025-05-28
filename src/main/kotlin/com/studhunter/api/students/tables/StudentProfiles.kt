package com.studhunter.api.students.tables

import com.studhunter.api.students.model.StudentProfile
import com.studhunter.api.students.repository.StudentProfileRepository
import com.studhunter.api.users.tables.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object StudentProfiles : Table("student_profiles"), StudentProfileRepository {
    private val userId = reference("user_id", Users.id).uniqueIndex()
    private val major = varchar("major", 100).nullable()
    private val course = integer("course").nullable()
    private val skills = text("skills").nullable()
    private val resumeUrl = varchar("resume_url", 255).nullable()
    private val portfolioUrl = varchar("portfolio_url", 255).nullable()

    override val primaryKey = PrimaryKey(userId)

    override fun insertProfile(profile: StudentProfile): Boolean? = try {
        transaction {
            insertIgnore {
                it[userId] = profile.userId
                it[major] = profile.major
                it[course] = profile.course
                it[skills] = profile.skills
                it[resumeUrl] = profile.resumeUrl
                it[portfolioUrl] = profile.portfolioUrl
            }.insertedCount > 0
        }
    } catch (e: Exception) { null }

    override fun getProfile(userId: String): StudentProfile? = try {
        transaction {
            select { StudentProfiles.userId eq UUID.fromString(userId) }
                .singleOrNull()
                ?.let {
                    StudentProfile(
                        userId = it[StudentProfiles.userId],
                        major = it[major],
                        course = it[course],
                        skills = it[skills],
                        resumeUrl = it[resumeUrl],
                        portfolioUrl = it[portfolioUrl]
                    )
                }
        }
    } catch (e: Exception) { null }

    override fun updateProfile(userId: String, profile: StudentProfile): Boolean? = try {
        transaction {
            update({ StudentProfiles.userId eq UUID.fromString(userId) }) {
                profile.major?.let { it1 -> it[major] = it1 }
                profile.course?.let { it1 -> it[course] = it1 }
                profile.skills?.let { it1 -> it[skills] = it1 }
                profile.resumeUrl?.let { it1 -> it[resumeUrl] = it1 }
                profile.portfolioUrl?.let { it1 -> it[portfolioUrl] = it1 }
            } > 0
        }
    } catch (e: Exception) { null }

    override fun deleteProfile(userId: String): Boolean? = try {
        transaction {
            deleteWhere { StudentProfiles.userId eq UUID.fromString(userId) } > 0
        }
    } catch (e: Exception) { null }

}