package com.studhunter.api.users.tables

import com.studhunter.api.users.model.User
import com.studhunter.api.users.responses.UserResponse
import com.studhunter.api.reviews.tables.Reviews
import com.studhunter.api.users.model.UserRole
import com.studhunter.api.users.repository.UsersRepository
import com.studhunter.api.users.requests.EditProfileRequest
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

object Users : Table(), UsersRepository {
    val id = this.uuid("id").autoGenerate()
    private val username = Users.varchar("username", 25).uniqueIndex()
    private val password = Users.varchar("password", 64)
    private val salt = Users.varchar("salt", 64)
    val rating = Users.double("rating")
    private val name = Users.varchar("name", 25)
    private val surname = Users.varchar("surname", 25).nullable()
    private val email = Users.varchar("email", 50).uniqueIndex()
    private val university = Users.varchar("university", 200).nullable()
    private val role = Users.enumerationByName("role", 20, UserRole::class)

    override val primaryKey = PrimaryKey(id)

    override fun insertUser(user: User): UUID? {
        return try {
            transaction {
                Users.insert {
                    it[id] = user.id
                    it[username] = user.username
                    it[password] = user.password
                    it[salt] = user.salt
                    it[rating] = user.rating
                    it[name] = user.name
                    it[surname] = user.surname
                    it[email] = user.email
                    it[university] = user.university
                    it[role] = user.role
                }
            }
            user.id
        } catch (e: Exception) {
            null
        }
    }

    override fun getUserByUsername(username: String): UserResponse? {
        return try {
            transaction {
                val user = Users.select { Users.username.eq(username) }.single()
                UserResponse(
                    id = user[Users.id].toString(),
                    username = user[Users.username],
                    email = user[email],
                    name = user[name],
                    surname = user[surname],
                    university = user[university],
                    rating = user[rating]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getUserByEmail(email: String): UserResponse? {
        return try {
            transaction {
                val user = Users.select { Users.email.eq(email) }.first()
                UserResponse(
                    id = user[Users.id].toString(),
                    username = user[username],
                    email = user[Users.email],
                    name = user[name],
                    surname = user[surname],
                    university = user[university],
                    rating = user[rating]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getUserDetailed(username: String): User? {
        return try {
            transaction {
                val user = Users.select { Users.username.eq(username) }.single()
                User(
                    id = user[Users.id],
                    username = user[Users.username],
                    email = user[email],
                    name = user[name],
                    surname = user[surname],
                    password = user[password],
                    salt = user[salt],
                    university = user[university],
                    role = user[role]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun editUser(userID: String, editProfileRequest: EditProfileRequest): Boolean? {
        return try {
            transaction {
                update({ Users.id.eq(UUID.fromString(userID)) }) {
                    it[name] = editProfileRequest.name
                    it[surname] = editProfileRequest.surname
                    it[university] = editProfileRequest.university
                } > 0
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getUserById(id: String): UserResponse? {
        return try {
            transaction {
                val user = Users.select { Users.id.eq(UUID.fromString(id)) }.single()
                UserResponse(
                    id = user[Users.id].toString(),
                    username = user[username],
                    email = user[email],
                    name = user[name],
                    surname = user[surname],
                    university = user[university],
                    rating = user[rating]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun updateRating(userId: String): Boolean? {
        return try {
            val reviews = Reviews.fetchUserReviews(userId).map { it.reviewValue }
            if (reviews.isEmpty()) return false
            transaction {
                val newRating = reviews.filterNotNull().sum() / reviews.count()
                update({ Users.id eq UUID.fromString(userId) }) {
                    it[rating] = newRating
                }
            }
            true
        } catch (e: Exception) {
            null
        }
    }

}
