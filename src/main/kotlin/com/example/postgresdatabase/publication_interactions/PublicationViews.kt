package com.example.postgresdatabase.publication_interactions

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object PublicationViews: Table() {
    private val pubId = varchar("publicationid", 12)
    private val username = varchar("username", 20)

    fun insertView(publicationId: String, username: String): Boolean {
        return try {
            transaction {
                insertIgnore {
                    it[pubId] = publicationId.substring(0, 12)
                    it[PublicationViews.username] = username
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun fetchViewCount(publicationId: String): Int {
        return try {
            transaction { select { pubId.eq(publicationId.substring(0, 12)) }.count().toInt() }
        } catch (e: Exception) { 0 }
    }

    private fun fetchAllViews(): List<Pair<String, String>>? {
        return try {
            transaction {
                val rows = selectAll().toList().map { row ->
                    row[pubId] to row[username]
                }
                rows
            }
        } catch (e: Exception) {
            null
        }
    }
}