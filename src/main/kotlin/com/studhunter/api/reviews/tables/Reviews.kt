package com.studhunter.api.reviews.tables

import com.studhunter.api.reviews.model.Review
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Reviews: Table() {
    private val id = varchar("id", 36)
    private val reviewerId = varchar("reviewer_id", 36)
    private val executorId = varchar("executor_id", 36)
    private val reviewValue = double("review_value").nullable()
    private val reviewMessage = varchar("review_message", 100).nullable()
    private val timestamp = long("timestamp")
    private val publicationId = varchar("publication_id", 36)

    fun insertReview(review: Review): Boolean? {
        return try {
            transaction {
                insert {
                    it[id] = review.id
                    it[reviewerId] = review.reviewerId
                    it[executorId] = review.executorId
                    it[reviewValue] = review.reviewValue
                    it[reviewMessage] = review.reviewMessage
                    it[timestamp] = review.timestamp
                    it[publicationId] = review.publicationId
                }.insertedCount > 0
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchUserReviews(userId: String): List<Review> {
        return try {
            transaction {
                select { Reviews.executorId.eq(userId).and(reviewValue.isNotNull()) }.map { row ->
                    Review(
                        id = row[Reviews.id],
                        reviewerId = row[reviewerId],
                        executorId = userId,
                        reviewValue = row[reviewValue],
                        reviewMessage = row[reviewMessage],
                        timestamp = row[timestamp],
                        publicationId = row[publicationId]
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fetchAuthorReviews(reviewAuthorId: String): List<Review> {
        return try {
            transaction {
                select { reviewerId.eq(reviewAuthorId).and(reviewValue.isNotNull()) }.map { row ->
                    Review(
                        id = row[Reviews.id],
                        reviewerId = row[reviewerId],
                        executorId = row[executorId],
                        reviewValue = row[reviewValue],
                        reviewMessage = row[reviewMessage],
                        timestamp = row[timestamp],
                        publicationId = row[publicationId]
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fetchPublicationReviews(publicationId: String): List<Review> {
        return try {
            transaction {
                select { this@Reviews.publicationId.eq(publicationId).and(reviewValue.isNotNull()) }.map { row ->
                    Review(
                        id = row[Reviews.id],
                        reviewerId = row[reviewerId],
                        executorId = row[executorId],
                        reviewValue = row[reviewValue],
                        reviewMessage = row[reviewMessage],
                        timestamp = row[timestamp],
                        publicationId = row[this@Reviews.publicationId]
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}