package com.example.postgresdatabase.common

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object PriceTypes : Table() {
    private val id = integer("id")
    private val name = varchar("name", 30)

    fun getPriceTypes(): Map<Int, String>? {
        return try {
            transaction {
                val priceTypes = PriceTypes.selectAll().associate {
                    it[PriceTypes.id] to it[name]
                }
                priceTypes
            }
        } catch (e: Exception) {
            null
        }
    }
}