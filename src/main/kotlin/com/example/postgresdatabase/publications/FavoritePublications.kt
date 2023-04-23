package com.example.postgresdatabase.publications

import com.example.data.models.Publication
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object FavoritePublications : Table() {
    private val userId = varchar("userid", 36)
    private val favorite = varchar("favorite", 36)

    fun fetchFavorites(uid: String): List<Publication>? {
        return try {
            transaction {
                val favorites = select { userId.eq(uid) }.toList().map { row ->
                    row[userId]
                }
                val publications = favorites.mapNotNull { Publications.fetchPublication(searchId = it) }
                publications
            }
        } catch (e: Exception) {
            null
        }
    }

    fun insertFavorite(uid: String, publicationId: String): Boolean {
        return try {
            transaction {
                insert {
                    it[userId] = uid
                    it[favorite] = publicationId
                }
            }
            true
        } catch (e: ExposedSQLException) {
            false
        }
    }

}