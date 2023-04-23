package com.example.postgresdatabase.users

import com.example.data.models.User
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Users: Table() {
    private val id = Users.varchar("id", 36)
    private val username = Users.varchar("username", 25)
    private val password = Users.varchar("password", 64)
    private val salt = Users.varchar("salt", 64)
    private val rating = Users.double("rating")
    private val fullName = Users.varchar("fullname", 25).nullable()
    private val email = Users.varchar("email", 50).nullable()

    fun insertUser(user: User) {
        transaction {
            Users.insert {
                it[id] = user.id
                it[username] = user.username
                it[password] = user.password
                it[salt] = user.salt
                it[rating] = user.rating
                it[fullName] = user.fullName
                it[email] = user.email
            }
        }
    }

    fun fetchUser(userName: String): User? {
        return try {
            transaction {
                val user = Users.select { username.eq(userName) }.single()
                User(
                    username = user[username],
                    password = user[password],
                    email = user[email],
                    fullName = user[fullName],
                    salt = user[salt]
                )
            }
        } catch (e: Exception) { null }
    }
}