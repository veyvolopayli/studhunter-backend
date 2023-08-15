package com.studhunter.api.reviews.tables

import com.studhunter.api.reviews.model.Review
import com.studhunter.api.reviews.requests.InsertReviewRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PSQLException

object Reviews: Table() {
    private val id = varchar("id", 36)
    private val reviewerId = varchar("reviewer_id", 36)
    private val userId = varchar("user_id", 36)
    private val review = double("review").nullable()
    private val reviewMessage = varchar("review_message", 100).nullable()
    private val timestamp = long("timestamp")
    private val publicationId = varchar("publication_id", 36)

    fun insertReview(reviewToInsert: InsertReviewRequest): Boolean? {
        return try {
            transaction {
                update({ Reviews.id.eq(reviewToInsert.id).and(review.eq(null)) }) {
                    it[review] = reviewToInsert.review
                    it[reviewMessage] = reviewToInsert.reviewMessage
                }
            }
            return true
        } catch (e: PSQLException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun fetchUserReviews(userId: String): List<Review> {
        return try {
            transaction {
                val reviews = select { Reviews.userId.eq(userId).and(review.isNotNull()) }.toList().map { row ->
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
                val reviews = select { reviewerId.eq(reviewAuthorId).and(review.isNotNull()) }.toList().map { row ->
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
                val reviews = select { publicationId.eq(neededPublicationId).and(review.isNotNull()) }.toList().map { row ->
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

    fun openNewReview(reviewToOpen: Review): String? {
        return try {
            transaction {
                insert {
                    it[id] = reviewToOpen.id
                    it[reviewerId] = reviewToOpen.reviewerId
                    it[userId] = reviewToOpen.userId
                    it[review] = reviewToOpen.review
                    it[reviewMessage] = reviewToOpen.reviewMessage
                    it[timestamp] = reviewToOpen.timestamp
                    it[publicationId] = reviewToOpen.publicationId
                }
            }
            return reviewToOpen.id
        } catch (e: PSQLException) {
            "You have already left a review for this publication"
        } catch (e: Exception) {
            null
        }
    }
}