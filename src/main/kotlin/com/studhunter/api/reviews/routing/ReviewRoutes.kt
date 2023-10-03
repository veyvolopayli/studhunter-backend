package com.studhunter.api.reviews.routing

import com.studhunter.api.chat.tables.Tasks
import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.reviews.model.Review
import com.studhunter.api.reviews.requests.NewReviewRequest
import com.studhunter.api.reviews.tables.Reviews
import com.studhunter.api.users.tables.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.insertReviews() {
    authenticate {
        post("reviews/new") {
            val reviewRequest = call.receiveNullable<NewReviewRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val currentUserId = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "JWT01. Invalid JWT.")
                return@post
            }

            val task = Tasks.getTaskById(taskId = reviewRequest.taskId) ?: run {
                call.respond(status = HttpStatusCode.BadRequest, message = "Task does not exist")
                return@post
            }

            if (currentUserId != task.customerId) {
                println("$currentUserId : ${task.customerId}")
                call.respond(status = HttpStatusCode.BadRequest, message = "No no no... Not today!")
                return@post
            }

            val review = Review(
                executorId = task.executorId,
                reviewerId = currentUserId,
                reviewValue = reviewRequest.reviewValue,
                reviewMessage = reviewRequest.reviewMessage,
                publicationId = task.publicationId
            )

            val reviewInserted = Reviews.insertReview(review)

            if (reviewInserted != true) {
                call.respond(status = HttpStatusCode.Conflict, message = "Failed to post a review")
            } else {
                Tasks.deleteTask(taskId = reviewRequest.taskId)
                call.respond(status = HttpStatusCode.OK, message = review)
            }
        }
    }
}

fun Route.fetchReviews() {
    authenticate {
        get("users/reviews/user/{userid}") {

            val userId = call.parameters["userid"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val userExists = Users.getUserById(userId) != null

            if (!userExists) {
                call.respond(status = HttpStatusCode.BadRequest, message = "User does not exist")
                return@get
            }

            val reviews = Reviews.fetchUserReviews(userId = userId)

            call.respond(status = HttpStatusCode.OK, message = reviews)
        }

        get("users/reviews/author/{authorid}") {

            val authorId = call.parameters["authorid"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val userExists = Users.getUserById(authorId) != null

            if (!userExists) {
                call.respond(status = HttpStatusCode.BadRequest, message = "User does not exist")
                return@get
            }

            val reviews = Reviews.fetchAuthorReviews(reviewAuthorId = authorId)

            call.respond(status = HttpStatusCode.OK, message = reviews)
        }
    }
}