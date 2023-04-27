package com.example.postgresdatabase.publicationinteractions

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object PublicationViews: Table() {
    private val hashedPubId = varchar("publicationid", 12)
    private val username = varchar("username", 20)

    fun insertView(publicationId: String, username: String): Boolean {
        return try {
            transaction {
                insertIgnore {
//                    it[hashedPubId] = publicationId.hashed(12)
                    it[hashedPubId] = publicationId.substring(0, 12)
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
            transaction {
//                val count = select { hashedPubId.eq(publicationId.hashed(12)) }.count().toInt()
                val count = select { hashedPubId.eq(publicationId.substring(0, 12)) }.count().toInt()
                count
            }
        } catch (e: Exception) { 0 }
    }

    private fun fetchAllViews(): List<Pair<String, String>>? {
        return try {
            transaction {
                val rows = selectAll().toList().map { row ->
                    row[hashedPubId] to row[username]
                }
                rows
            }
        } catch (e: Exception) {
            null
        }
    }
}