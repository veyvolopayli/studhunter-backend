package com.studhunter.api.favorites.tables

import com.studhunter.api.favorites.model.FavoritePublication
import com.studhunter.api.publications.model.Publication
import com.studhunter.api.publications.tables.Publications
import com.studhunter.api.users.repository.FavoritePublicationsRepository
import io.ktor.util.date.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object FavoritePublications : Table("favorite_publications"), FavoritePublicationsRepository {
    private val userId = varchar("user_id", 36)
    private val favoritePubId = varchar("publication_id", 36)

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

    override fun isInFavorites(userID: String, pubID: String): Boolean? {
        return try {
            transaction {
                select { userId.eq(userID) and favoritePubId.eq(pubID) }.count() > 0
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun fetchPubInFavoritesCount(publicationId: String): Long {
        return try {
            transaction { select { favoritePubId.eq(publicationId) }.count() }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun insertFavorite(uid: String, publicationId: String): Boolean? {
        return try {
            transaction {
                insertIgnore {
                    it[userId] = uid
                    it[favoritePubId] = publicationId
                }.insertedCount != 0
            }
        } catch (e: ExposedSQLException) {
            null
        }
    }

    override fun removeFavorite(uid: String, publicationId: String): Boolean? {
        return try {
            transaction {
                val count = deleteWhere { userId.eq(uid) and favoritePubId.eq(publicationId) }
                count != 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun getAllFavorites(): List<FavoritePublication>? {
        return try {
            transaction {
                selectAll().map {
                    FavoritePublication(
                        favoritePubID = it[favoritePubId],
                        userID = it[userId]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun deleteFavorites(favoritesToRemove: Set<FavoritePublication>): Boolean? {
        return try {
            transaction {
                favoritesToRemove.forEach { favorite ->
                    deleteWhere { userId.eq(favorite.userID) and favoritePubId.eq(favorite.favoritePubID) }
                }
                true
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun insertFavorites(favorites: Set<FavoritePublication>): Boolean? {
        return try {
            transaction {
                batchInsert(favorites) { favorite ->
                    this[userId] = favorite.userID
                    this[favoritePubId] = favorite.favoritePubID
                }.isNotEmpty()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun synchronizeDatabaseWithList(favPublications: Set<FavoritePublication>): Boolean? {
        val favoritesFromDB = getAllFavorites() ?: emptyList()

        if (favPublications.isEmpty()) return true

        val toDelete = favoritesFromDB.toSet() - favPublications
        val toAdd = favPublications - favoritesFromDB.toSet()

        deleteFavorites(toDelete)?.let {
            insertFavorites(toAdd)
        } ?: return null

        return true
    }

}