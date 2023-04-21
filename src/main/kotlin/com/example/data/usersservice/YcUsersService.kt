package com.example.data.usersservice

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.PutObjectResult
import com.example.BUCKET_NAME
import com.example.data.constants.USERS_DATA_PATH
import com.example.data.models.User
import com.example.data.models.UserReviews
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Timer
import java.util.TimerTask

class YcUsersService(private val s3: AmazonS3): UsersService {
    override suspend fun insertNewReview(userId: String, review: Float): Boolean {
        val result: PutObjectResult?
        val path = "$USERS_DATA_PATH/$userId/user_reviews.json"
        val reviews = getUserReviews(userId) ?: kotlin.run {
            val newListOfReviews = mutableListOf(review)
            val newJson = Gson().toJson(UserReviews(newListOfReviews))
            result = s3.putObject(BUCKET_NAME, path, newJson)
            return result != null
        }

        reviews.reviews.add(review)

        val updatedJson = Gson().toJson(reviews)

        result = s3.putObject(BUCKET_NAME, path, updatedJson)

        return result != null
    }

    override suspend fun calculateReview(userReviews: UserReviews?): Float {
        val reviews = userReviews?.reviews ?: return 5.0F
        val count = reviews.count()
        val sum = reviews.sum()
        return sum / count
    }

    override suspend fun insertCalculatedReviews(): Boolean {
        val listObjRequest = ListObjectsV2Request().withBucketName(BUCKET_NAME).withPrefix("users/")
        val objSummaries = s3.listObjectsV2(listObjRequest).objectSummaries
        val users = objSummaries.mapNotNull { summary ->
            val obj = s3.getObject(BUCKET_NAME, summary.key)
            val objContent = obj.objectContent.bufferedReader().use { it.readText() }
            val user = Gson().fromJson(objContent, User::class.java)
            user
        }

        val results = mutableListOf<PutObjectResult?>()
        users.forEach { user ->
            val path = "users/${user.username}.json"
            val userReviews = getUserReviews(user.id) ?: return@forEach
            val calcRev = calculateReview(userReviews)
            user.rating = calcRev
            val newUserProfile = Gson().toJson(user)
            results.add(s3.putObject(BUCKET_NAME, path, newUserProfile))
        }

        return !results.contains(null)
    }

    override suspend fun getUserReviews(userId: String): UserReviews? {
        val reviewsObject = try {
            s3.getObject(BUCKET_NAME, "$USERS_DATA_PATH/$userId/user_reviews.json")
        } catch (e: Exception) {
            return null
        }

        val reviewsJson = reviewsObject.objectContent.bufferedReader().use { it.readText() }

        return Json.decodeFromString<UserReviews>(reviewsJson)
    }

    override suspend fun startReviewsTask() {
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch { insertCalculatedReviews() }
            }
        }
        val hourInMills = 1000L * 60L * 10L
        val delay = 0L
        timer.scheduleAtFixedRate(task, delay, hourInMills)
    }
}