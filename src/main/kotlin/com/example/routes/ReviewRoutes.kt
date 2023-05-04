package com.example.routes

import com.example.data.models.Review
import com.example.data.models.User
import com.example.data.requests.InsertReviewRequest
import com.example.postgresdatabase.reviews.Reviews
import com.example.postgresdatabase.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.insertReviews() {
    authenticate {
        post("users/reviews/new") {
            val request = call.receiveNullable<InsertReviewRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val authorId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.Unauthorized, "Your ID is invalid")
                return@post
            }

            val userId = request.userId

            val userExists = Users.fetchUserById(userId = userId) != null

            if (!userExists) {
                call.respond(status = HttpStatusCode.BadRequest, message = "User does not exist")
                return@post
            }

            val review = Review(
                userId = userId,
                reviewerId = authorId,
                review = request.review,
                reviewMessage = request.reviewMessage,
                publicationId = request.publicationId
            )

            val newReviewId = Reviews.insertReview(leavedReview = review) ?: kotlin.run {
                call.respond(status = HttpStatusCode.Conflict, "Failed to insert new review")
                return@post
            }

            Users.updateRating(userId)

            call.respond(status = HttpStatusCode.OK, message = newReviewId)
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

            val userExists = Users.fetchUserById(userId = userId) != null

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

            val userExists = Users.fetchUserById(userId = authorId) != null

            if (!userExists) {
                call.respond(status = HttpStatusCode.BadRequest, message = "User does not exist")
                return@get
            }

            val reviews = Reviews.fetchAuthorReviews(reviewAuthorId = authorId)

            call.respond(status = HttpStatusCode.OK, message = reviews)
        }
    }
}