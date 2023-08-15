package com.studhunter.api.publications.tables

import com.studhunter.api.publications.repository.PublicationViewsRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object PublicationViews: Table("publication_views"), PublicationViewsRepository {
    private val pubId = varchar("publicationid", 12)
    private val username = varchar("username", 20)

    override fun insertView(publicationId: String, username: String): Boolean {
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

    override fun fetchViewsCount(publicationId: String): Int {
        return try {
            transaction { select { pubId.eq(publicationId.substring(0, 12)) }.count().toInt() }
        } catch (e: Exception) { 0 }
    }

}