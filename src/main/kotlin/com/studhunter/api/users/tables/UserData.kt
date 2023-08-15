package com.studhunter.api.users.tables

import com.studhunter.api.users.model.UserDataModel
import com.studhunter.api.users.repository.UserDataRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

object UserData : Table("user_data"), UserDataRepository {
    private val userId = varchar("userid", 36)
    private val joinDate = long("join_date")
    private val emailConfirmed = bool("email_confirmed")
    private val confirmationCode = integer("confirmation_code")

    override fun insertUserData(userDataModel: UserDataModel): Int? {
        return try {
            transaction {
                insertIgnore {
                    it[userId] = userDataModel.userId
                    it[joinDate] = userDataModel.joinDate
                    it[emailConfirmed] = userDataModel.emailConfirmed
                    it[confirmationCode] = userDataModel.confirmationCode
                }
                userDataModel.confirmationCode
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun fetchUserEmailConfirmed(userId: String): Boolean? {
        return try {
            transaction {
                val userDataModel = select { UserData.userId.eq(userId) }.single()
                userDataModel[emailConfirmed]
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun confirmEmail(userId: String, code: Int): Boolean? {
        return try {
            transaction {
                val affectedRows = update({
                    (UserData.userId eq userId) and (confirmationCode eq code) and (emailConfirmed eq false)
                }) {
                    it[emailConfirmed] = true
                }
                if (affectedRows > 0) true else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun updateConfirmationCode(userId: String): Int? {
        val newCode = Random.nextInt(333333, 999999)
        return try {
            transaction {
                update({ UserData.userId.eq(userId) }) {
                    it[confirmationCode] = newCode
                }
            }
            newCode
        } catch (e: Exception) {
            null
        }
    }
}