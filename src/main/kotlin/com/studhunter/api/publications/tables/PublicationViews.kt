package com.studhunter.api.publications.tables

import com.studhunter.api.publications.repository.PublicationViewsRepository
import io.ktor.util.date.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object PublicationViews: Table("publication_views"), PublicationViewsRepository {
    private val pubId = varchar("publication_id", 36)
    private val username = varchar("viewer_id", 36)
    private val timestamp = long("timestamp")

    override fun insertView(publicationId: String, username: String): Boolean? {
        return try {
            transaction {
                insertIgnore {
                    it[pubId] = publicationId
                    it[PublicationViews.username] = username
                    it[timestamp] = getTimeMillis()
                }.insertedCount > 0
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun fetchViewsCount(publicationId: String): Long {
        return try {
            transaction { select { pubId.eq(publicationId) }.count() }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

}