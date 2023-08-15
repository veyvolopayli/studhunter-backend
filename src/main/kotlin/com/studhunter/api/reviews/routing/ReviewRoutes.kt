package com.studhunter.api.reviews.routing

import com.studhunter.api.reviews.model.Review
import com.studhunter.api.reviews.requests.OpenReviewRequest
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
        post("users/reviews/new") {
            val request = call.receiveNullable<OpenReviewRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val authorId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.Unauthorized, "Your ID is invalid")
                return@post
            }

            val userId = request.userId

            val userExists = Users.getUserById(userId) != null

            if (!userExists) {
                call.respond(status = HttpStatusCode.BadRequest, message = "User does not exist")
                return@post
            }

            val review = Review(
                userId = userId,
                reviewerId = authorId,
                review = null,
                reviewMessage = null,
                publicationId = request.publicationId
            )

            val newReviewId = Reviews.openNewReview(reviewToOpen = review) ?: kotlin.run {
                call.respond(status = HttpStatusCode.Conflict, "Failed to insert new review")
                return@post
            }

//            Users.updateRating(userId)

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