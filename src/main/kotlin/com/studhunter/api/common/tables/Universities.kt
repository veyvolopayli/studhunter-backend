package com.studhunter.api.common.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Universities : Table() {
    private val name = varchar("name", 500)

    fun getUniversities(): List<String>? {
        return try {
            transaction {
                selectAll().map { it[name] }
            }
        } catch (e: Exception) {
            null
        }
    }
}