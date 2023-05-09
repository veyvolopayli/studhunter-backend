package com.example.postgresdatabase.users

import com.example.data.models.User
import com.example.data.responses.UserResponse
import com.example.postgresdatabase.reviews.Reviews
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object Users : Table() {
    private val id = Users.varchar("id", 36)
    private val username = Users.varchar("username", 25)
    private val password = Users.varchar("password", 64)
    private val salt = Users.varchar("salt", 64)
    private val rating = Users.double("rating")
    private val name = Users.varchar("name", 25)
    private val surname = Users.varchar("surname", 25).nullable()
    private val email = Users.varchar("email", 50)
    private val university = Users.varchar("university", 30).nullable()

    fun insertUser(user: User) {
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
            }
        }
    }

    fun fetchUser(userName: String): UserResponse? {
        return try {
            transaction {
                val user = Users.select { username.eq(userName) }.single()
                UserResponse(
                    id = user[Users.id],
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

    fun fetchUserDetailed(userName: String): User? {
        return try {
            transaction {
                val user = Users.select { username.eq(userName) }.single()
                User(
                    id = user[Users.id],
                    username = user[username],
                    email = user[email],
                    name = user[name],
                    surname = user[surname],
                    password = user[password],
                    salt = user[salt],
                    university = user[university]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchUserById(userId: String): UserResponse? {
        return try {
            transaction {
                val user = Users.select { Users.id.eq(userId) }.single()
                UserResponse(
                    id = user[Users.id],
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
        val reviews = Reviews.fetchUserReviews(userId).map { it.review }
        if (reviews.isNotEmpty()) {
            return try {
                transaction {
                    val newRating = reviews.filterNotNull().sum() / reviews.count()
                    update({ Users.id eq userId }) {
                        it[rating] = newRating
                    }
                }
                true
            } catch (e: Exception) {
                null
            }
        }
        return true
    }

}