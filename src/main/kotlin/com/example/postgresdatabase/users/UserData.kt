package com.example.postgresdatabase.users

import com.example.data.models.UserDataModel
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import kotlin.random.Random

object UserData: Table() {
    private val userId = varchar("userid", 36)
    private val joinDate = long("join_date")
    private val emailConfirmed = bool("email_confirmed")
    private val confirmationCode = integer("confirmation_code")

    fun insertUserData(userDataModel: UserDataModel): Int? {
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
        } catch (e: Exception) { null }
    }

    fun confirm(userId: String, code: Int): Boolean? {
        return try {
            transaction {
                update({
                    UserData.userId.eq(userId)
                    confirmationCode.eq(code)
                }) {
                    it[emailConfirmed] = true
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateConfirmationCode(userId: String): Int? {
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