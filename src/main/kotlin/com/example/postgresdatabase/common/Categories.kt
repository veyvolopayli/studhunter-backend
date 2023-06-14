package com.example.postgresdatabase.common

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Categories : Table() {
    private val id = integer("id")
    private val name = varchar("name", 30)

    fun getCategories(): Map<Int, String>? {
        return try {
            transaction {
                val categories = Categories.selectAll().associate {
                    it[Categories.id] to it[name]
                }
                categories
            }
        } catch (e: Exception) {
            null
        }
    }
}