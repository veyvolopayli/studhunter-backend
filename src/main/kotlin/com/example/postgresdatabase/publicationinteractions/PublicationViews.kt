package com.example.postgresdatabase.publicationinteractions

import com.example.features.hashed
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object PublicationViews: Table() {
    private val hashedPubId = varchar("hashedPubId", 12)
    private val username = varchar("username", 20)

    fun insertView(publicationId: String, username: String): Boolean {
        return try {
            if (fetchAllViews()?.contains(publicationId.hashed(12) to username) ?: return false) return false
            transaction {
                insert {
                    it[hashedPubId] = publicationId.hashed(12)
                    it[PublicationViews.username] = username
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun fetchViewsCount(publicationId: String): Int? {
        return try {
            transaction {
                val count = select { hashedPubId.eq(publicationId.hashed(12)) }.count().toInt()
                count
            }
        } catch (e: Exception) {
            null
        }
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