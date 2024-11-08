package com.studhunter.api.publications.tables

import com.studhunter.api.common.nullifyEmptyList
import com.studhunter.api.publications.model.Publication
import com.studhunter.api.publications.repository.PublicationsRepository
import com.studhunter.api.publications_filter.model.FilterRequest
import com.studhunter.api.users.tables.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Publications : Table(), PublicationsRepository {
    val id = Publications.varchar("id", 36)
    private val imageUrl = Publications.varchar("imageurl", 120)
    val title = Publications.varchar("title", 50)
    private val description = Publications.varchar("description", 1500)
    private val price = Publications.integer("price").nullable()
    private val priceType = Publications.varchar("pricetype", 20)
    private val district = Publications.varchar("district", 50).nullable()
    private val timestamp = Publications.long("timestamp")
    private val category = Publications.varchar("category", 50)
    private val userId = Publications.varchar("userid", 36)
    private val socials = Publications.varchar("socials", 20)
    private val approved = Publications.bool("approved").nullable()

    override fun insertPublication(publication: Publication): String? {
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

    override fun getPublicationsByCategory(category: String): List<Publication>? {
        return try {
            transaction {
                val publications = Publications.select { Publications.category.eq(category) }.map { row ->
                    Publication(
                        id = row[Publications.id],
                        imageUrl = row[imageUrl],
                        title = row[title],
                        description = row[description],
                        price = row[price],
                        priceType = row[priceType],
                        district = row[district],
                        timestamp = row[timestamp],
                        category = row[Publications.category],
                        userId = row[userId],
                        socials = row[socials],
                        approved = row[approved]
                    )
                }
                publications.sortedBy { it.timestamp }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getPublicationsByDistrict(district: String): List<Publication>? {
        return try {
            transaction {
                Publications.select { Publications.district.eq(district) }.map { row ->
                    Publication(
                        id = row[Publications.id],
                        imageUrl = row[imageUrl],
                        title = row[title],
                        description = row[description],
                        price = row[price],
                        priceType = row[priceType],
                        district = row[Publications.district],
                        timestamp = row[timestamp],
                        category = row[category],
                        userId = row[userId],
                        socials = row[socials],
                        approved = row[approved]
                    )
                }.sortedBy { it.timestamp }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getClosestPublications(district: String): List<Publication> {
        TODO("Not yet implemented")
    }

    override fun getPublication(id: String): Publication? {
        return try {
            transaction {
                val publication = Publications.select { Publications.id.eq(id) }.single()
                Publication(
                    id = id,
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

    override fun getAllPublications(): List<Publication>? {
        return try {
            transaction {
                val publications = Publications.selectAll().map { row ->
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
                publications.sortedBy { it.timestamp }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getPublicationsByQuery(query: String): List<Publication>? {
        return try {
            val lowerQuery = query.lowercase()
            transaction {
                Publications.select { (title.lowerCase() like "%$lowerQuery%") or (description.lowerCase() like "%$lowerQuery%") }
                    .map { row ->
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
                    }.sortedBy { it.timestamp }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getPublicationsByUserId(userId: String): List<Publication>? {
        return try {
            transaction {
                val publications = Publications.select { Publications.userId.eq(userId) }.map { row ->
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
                        userId = row[Publications.userId],
                        socials = row[socials],
                        approved = row[approved]
                    )
                }
                publications.sortedBy { it.timestamp }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun updatePublicationStatus(pubId: String, approve: Boolean): Boolean? {
        return try {
            transaction {
                Publications.update({ Publications.id.eq(pubId) }) {
                    it[approved] = approve
                }
            }
            return approve
        } catch (e: Exception) {
            null
        }
    }

    override fun deletePublication(publicationId: String): Int? {
        return try {
            transaction {
                Publications.deleteWhere {
                    id eq publicationId
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun deleteUserPublications(userID: String): Int? {
        return try {
            transaction {
                deleteWhere {
                    this.userId.eq(userID)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getFilteredPublications(filter: FilterRequest): List<Publication>? {
        if (filter.priceTypes.isNullOrEmpty() && filter.maxPrice == null && filter.minPrice == null && filter.categories.isNullOrEmpty() && filter.districts.isNullOrEmpty() && filter.minUserRating == null) {
            return getAllPublications()
        }

        return try {
            transaction {

                val condition = userId eq Users.userId

                val result = join(Users, JoinType.INNER, additionalConstraint = { condition }).select {
                    approved.eq(true) or approved.eq(false) or approved.eq(null)

                }.also { q ->
                    filter.minUserRating?.let { minRating ->
                        q.andWhere { Users.rating.greaterEq(minRating.toDouble()) }
                    }

                    filter.minPrice?.let { minPrice ->
                        q.andWhere { price.greaterEq(minPrice) }
                    }

                    filter.maxPrice?.let { maxPrice ->
                        q.andWhere { price.lessEq(maxPrice) }
                    }

                    filter.priceTypes?.nullifyEmptyList()?.let { priceTypes ->
                        q.andWhere { priceType.inList(priceTypes) }
                    }

                    filter.districts?.nullifyEmptyList()?.let { districts ->
                        q.andWhere { district.inList(districts) }
                    }

                    filter.categories?.nullifyEmptyList()?.let { categories ->
                        q.andWhere { category.inList(categories) }
                    }
                }.map {
                    Publication(
                        it[Publications.id],
                        it[Publications.imageUrl],
                        it[Publications.title],
                        it[Publications.description],
                        it[Publications.price],
                        it[Publications.priceType],
                        it[Publications.district],
                        it[Publications.timestamp],
                        it[Publications.category],
                        it[Publications.userId],
                        it[Publications.socials],
                        it[Publications.approved]
                    )
                }

                return@transaction result
            }
        } catch (e: Exception) {
            null
        }
    }

}