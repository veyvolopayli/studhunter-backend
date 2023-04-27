package com.example.postgresdatabase.publications

import com.example.data.models.Publication
import com.example.features.getCurrentMills
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object FavoritePublications : Table() {
    private val userId = varchar("userid", 36)
    private val favoritePubId = varchar("fav_pubid", 36)

    fun fetchFavorites(uid: String): List<Publication> {
        return try {
            transaction {
                val favorites = select { userId.eq(uid) }.toList().map { row ->
                    row[userId]
                }
                val publications = favorites.mapNotNull { Publications.fetchPublication(searchId = it) }
                publications
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fetchPubInFavoritesCount(publicationId: String): Int {
        return try {
            transaction { select { favoritePubId.eq(publicationId) }.count().toInt() }
        } catch (e: Exception) { 0 }
    }

    fun insertFavorite(uid: String, publicationId: String): Boolean? {
        return try {
            transaction {
                insert {
                    it[userId] = uid
                    it[favoritePubId] = publicationId
                }
            }
            true
        } catch (e: ExposedSQLException) {
            null
        }
    }

    fun removeFavorite(uid: String, publicationId: String): Boolean? {
        return try {
            transaction {
                deleteWhere { userId.eq(uid) and favoritePubId.eq(publicationId) }
            }
            true
        } catch (e: ExposedSQLException) {
            null
        }
    }

    fun removeAllUserFavorites(uid: String): Boolean? {
        return try {
            transaction {
                deleteWhere { userId.eq(uid) }
            }
            true
        } catch (e: ExposedSQLException) {
            null
        }
    }

}