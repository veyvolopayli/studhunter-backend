package com.studhunter.api.users.tables

import com.studhunter.api.publications.model.Publication
import com.studhunter.api.publications.tables.Publications
import com.studhunter.api.users.repository.FavoritePublicationsRepository
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object FavoritePublications : Table("favorite_publications"), FavoritePublicationsRepository {
    private val userId = varchar("userid", 36)
    private val favoritePubId = varchar("fav_pubid", 36)

    override fun fetchFavorites(uid: String): List<Publication> {
        return try {
            transaction {
                val favorites = select { userId.eq(uid) }.map { row ->
                    row[favoritePubId]
                }
                val publications = favorites.mapNotNull { Publications.getPublication(id = it) }
                publications
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun checkFavorite(userID: String, pubID: String): Unit? {
        return try {
            transaction {
                select { userId.eq(userID) and favoritePubId.eq(pubID) }.first()
            }
            Unit
        } catch (e: Exception) {
            null
        }
    }

    override fun fetchPubInFavoritesCount(publicationId: String): Int {
        return try {
            transaction { select { favoritePubId.eq(publicationId) }.count().toInt() }
        } catch (e: Exception) { 0 }
    }

    override fun insertFavorite(uid: String, publicationId: String): Boolean? {
        return try {
            transaction {
                insertIgnore {
                    it[userId] = uid
                    it[favoritePubId] = publicationId
                }
            }
            true
        } catch (e: ExposedSQLException) {
            null
        }
    }

    override fun removeFavorite(uid: String, publicationId: String): Boolean? {
        return try {
            transaction {
                deleteWhere { userId.eq(uid) and favoritePubId.eq(publicationId) }
            }
            true
        } catch (e: ExposedSQLException) {
            null
        }
    }

    override fun removeAllUserFavorites(uid: String): Boolean? {
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