package com.example.postgresdatabase.reviews

import com.example.data.models.Review
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Reviews: Table() {
    private val id = varchar("id", 36)
    private val reviewerId = varchar("reviewerid", 36)
    private val userId = varchar("userid", 36)
    private val review = double("review")
    private val reviewMessage = varchar("reviewmessage", 36).nullable()
    private val timestamp = long("timestamp")

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
                }
            }
            return leavedReview.id
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
                        timestamp = row[timestamp]
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
                        timestamp = row[timestamp]
                    )
                }
                reviews
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}