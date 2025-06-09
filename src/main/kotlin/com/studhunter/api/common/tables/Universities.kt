package com.studhunter.api.common.tables

import com.studhunter.api.common.model.University
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.Serializable

object Universities : Table() {
    private val id = uuid("id").autoGenerate()
    private val name = varchar("name", 500)
    private val shortname = varchar("shortname", 100)

    override val primaryKey = PrimaryKey(id)

    fun getUniversities(): List<University>? {
        return try {
            transaction {
                selectAll().map {
                    University(
                        id = it[Universities.id].toString(),
                        name = it[name],
                        shortName = it[shortname]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

}
