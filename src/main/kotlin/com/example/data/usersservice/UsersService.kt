package com.example.data.usersservice

import com.example.data.models.UserReviews

interface UsersService {
    suspend fun insertNewReview(userId: String, review: Double): Boolean

    suspend fun calculateReview(userReviews: UserReviews?): Double

    suspend fun insertCalculatedReviews(): Boolean

    suspend fun getUserReviews(userId: String): UserReviews?

    suspend fun startReviewsTask()
}