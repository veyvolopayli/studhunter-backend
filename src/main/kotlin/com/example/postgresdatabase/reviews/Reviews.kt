package com.example.postgresdatabase.reviews

import com.example.data.models.Review
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PSQLException

object Reviews: Table() {
    private val id = varchar("id", 36)
    private val reviewerId = varchar("reviewer_id", 36)
    private val userId = varchar("user_id", 36)
    private val review = double("review")
    private val reviewMessage = varchar("review_message", 100).nullable()
    private val timestamp = long("timestamp")
    private val publicationId = varchar("publication_id", 36)

    fun insertReview(leavedReview: Review): String? {
        return try {
            transaction {
                insert {
                    it[id] = leavedReview.id
                    it[reviewerId] = leavedReview.reviewerId
                    it[userId] = leavedReview.userId
                    it[review] = leavedReview.review
                    it[reviewMessage] = leavedReview.reviewMessage
                    it[timestamp] = leavedReview.timestamp
                    it[publicationId] = leavedReview.publicationId
                }
            }
            return leavedReview.id
        } catch (e: PSQLException) {
            "You have already left a review for this publication"
        } catch (e: Exception) {
            null
        }
    }

    fun fetchUserReviews(userId: String): List<Review> {
        return try {
            transaction {
                val reviews = select { Reviews.userId.eq(userId) }.toList().map { row ->
                    Review(
                        id = row[Reviews.id],
                        reviewerId = row[reviewerId],
                        userId = userId,
                        review = row[review],
                        reviewMessage = row[reviewMessage],
                        timestamp = row[timestamp],
                        publicationId = row[publicationId]
                    )
                }
                reviews
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fetchAuthorReviews(reviewAuthorId: String): List<Review> {
        return try {
            transaction {
                val reviews = select { Reviews.reviewerId.eq(reviewAuthorId) }.toList().map { row ->
                    Review(
                        id = row[Reviews.id],
                        reviewerId = row[reviewerId],
                        userId = row[userId],
                        review = row[review],
                        reviewMessage = row[reviewMessage],
                        timestamp = row[timestamp],
                        publicationId = row[publicationId]
                    )
                }
                reviews
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fetchPublicationReviews(neededPublicationId: String): List<Review> {
        return try {
            transaction {
                val reviews = select { publicationId.eq(neededPublicationId) }.toList().map { row ->
                    Review(
                        id = row[Reviews.id],
                        reviewerId = row[reviewerId],
                        userId = row[userId],
                        review = row[review],
                        reviewMessage = row[reviewMessage],
                        timestamp = row[timestamp],
                        publicationId = row[publicationId]
                    )
                }
                reviews
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}