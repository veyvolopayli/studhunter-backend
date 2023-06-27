package com.example.postgresdatabase.publication_place

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Districts : Table() {
    private val name = varchar("name", 50)

    fun getDistricts(): List<String>? {
        return try {
            transaction {
                val districts = Districts.selectAll().map { row ->
                    row[name]
                }
                districts.sorted()
            }
        } catch (e: Exception) {
            null
        }
    }
}

