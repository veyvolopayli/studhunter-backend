package com.example.postgresdatabase.publications

import com.example.data.models.Publication
import com.example.repositories.PublicationRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Publications : Table(), PublicationRepository {
    private val id = Publications.varchar("id", 36)
    private val imageUrl = Publications.varchar("imageurl", 100)
    private val title = Publications.varchar("title", 50)
    private val description = Publications.varchar("description", 1500)
    private val price = Publications.integer("price")
    private val priceType = Publications.integer("pricetype")
    private val district = Publications.varchar("district", 50).nullable()
    private val timestamp = Publications.varchar("timestamp", 30)
    private val category = Publications.varchar("category", 50)
    private val userId = Publications.varchar("userid", 36)
    private val socials = Publications.varchar("socials", 20)
    private val approved = Publications.bool("approved").nullable()

    override suspend fun insertPublication(publication: Publication): String? {
        try {
            transaction {
                Publications.insert {
                    it[id] = publication.id
                    it[imageUrl] = publication.imageUrl
                    it[title] = publication.title
                    it[description] = publication.description
                    it[price] = publication.price
                    it[priceType] = publication.priceType
                    it[district] = publication.district
                    it[timestamp] = publication.timestamp
                    it[category] = publication.category
                    it[userId] = publication.userId
                    it[socials] = publication.socials
                    it[approved] = publication.approved
                }
            }
            return publication.id
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun getPublication(id: String): Publication {
        TODO("Not yet implemented")
    }

    override suspend fun getAllPublications(): List<Publication> {
        TODO("Not yet implemented")
    }

    override suspend fun getPublicationsByQuery(query: String): List<Publication> {
        TODO("Not yet implemented")
    }

    override suspend fun getPublicationsByCategory(category: String): List<Publication> {
        TODO("Not yet implemented")
    }

    override suspend fun getPublicationsByUserId(userId: String): List<Publication> {
        TODO("Not yet implemented")
    }

    override suspend fun getPublicationsByDistrict(district: String): List<Publication> {
        TODO("Not yet implemented")
    }

    override suspend fun getClosestPublications(district: String): List<Publication> {
        TODO("Not yet implemented")
    }

    fun fetchPublication(searchId: String): Publication? {
        return try {
            transaction {
                val publication = Publications.select { Publications.id.eq(searchId) }.single()
                Publication(
                    id = searchId,
                    imageUrl = publication[imageUrl],
                    title = publication[title],
                    description = publication[description],
                    price = publication[price],
                    priceType = publication[priceType],
                    district = publication[district],
                    timestamp = publication[timestamp],
                    category = publication[category],
                    userId = publication[userId],
                    socials = publication[socials],
                    approved = publication[approved]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchAllPublications(): List<Publication>? {
        return try {
            transaction {
                val publications = Publications.selectAll().toList().map { row ->
                    Publication(
                        id = row[Publications.id],
                        imageUrl = row[imageUrl],
                        title = row[title],
                        description = row[description],
                        price = row[price],
                        priceType = row[priceType],
                        district = row[district],
                        timestamp = row[timestamp],
                        category = row[category],
                        userId = row[userId],
                        socials = row[socials],
                        approved = row[approved]
                    )
                }
                publications
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchPublicationsByCategory(searchCategory: String): List<Publication>? {
        return try {
            transaction {
                val publications = Publications.select { category.eq(searchCategory) }.toList().map { row ->
                    Publication(
                        id = row[Publications.id],
                        imageUrl = row[imageUrl],
                        title = row[title],
                        description = row[description],
                        price = row[price],
                        priceType = row[priceType],
                        district = row[district],
                        timestamp = row[timestamp],
                        category = row[category],
                        userId = row[userId],
                        socials = row[socials],
                        approved = row[approved]
                    )
                }
                publications
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchPublicationsByDistrict(searchDistrict: String): List<Publication>? {
        return try {
            transaction {
                val publications = Publications.select { district.eq(searchDistrict) }.toList().map { row ->
                    Publication(
                        id = row[Publications.id],
                        imageUrl = row[imageUrl],
                        title = row[title],
                        description = row[description],
                        price = row[price],
                        priceType = row[priceType],
                        district = row[district],
                        timestamp = row[timestamp],
                        category = row[category],
                        userId = row[userId],
                        socials = row[socials],
                        approved = row[approved]
                    )
                }
                publications
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchPublicationsByQuery(query: String): List<Publication>? {
        return fetchAllPublications()?.filter {
            it.title.contains(query) || it.description.contains(query) || it.category.contains(query)
        }
    }

    fun fetchPublicationsByUserId(searchUserId: String): List<Publication>? {
        return try {
            transaction {
                val publications = Publications.select { userId.eq(searchUserId) }.toList().map { row ->
                    Publication(
                        id = row[Publications.id],
                        imageUrl = row[imageUrl],
                        title = row[title],
                        description = row[description],
                        price = row[price],
                        priceType = row[priceType],
                        district = row[district],
                        timestamp = row[timestamp],
                        category = row[category],
                        userId = row[userId],
                        socials = row[socials],
                        approved = row[approved]
                    )
                }
                publications
            }
        } catch (e: Exception) {
            null
        }
    }

    fun updatePublicationStatus(pubId: String, approve: Boolean): Boolean? {
        return try {
            transaction {
                Publications.update ({ Publications.id.eq(pubId) }) {
                    it[approved] = approve
                }
            }
            return approve
        } catch (e: Exception) { null }
    }

    suspend fun <T> insertPublication(data: T) {
        TODO("Not yet implemented")
    }

}