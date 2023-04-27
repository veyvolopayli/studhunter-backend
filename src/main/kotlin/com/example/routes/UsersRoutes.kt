package com.example.routes

import com.example.data.usersservice.UsersService
import com.example.postgresdatabase.publications.Publications
import com.example.postgresdatabase.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.insertNewRating(usersService: UsersService) {
    authenticate {
        /*post("users/{id}/new-review") {
            val userId = call.parameters["id"] ?: kotlin.run {
                call.respond(status = HttpStatusCode.BadRequest, message = "Invalid link")
                return@post
            }

            val request = call.receiveNullable<NewReviewRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val review = request.review

            val inserted = usersService.insertNewReview(userId = userId, review = review)

            if (!inserted) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }*/

        get("users/{id}") {
            val userId = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val user = Users.fetchUserById(userId = userId) ?: kotlin.run {
                call.respond(status = HttpStatusCode.BadRequest, message = "User not found")
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = user)
        }

        get("users/publications/{id}") {
            val userId = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val publications = Publications.fetchPublicationsByUserId(searchUserId = userId) ?: kotlin.run {
                call.respond(status = HttpStatusCode.BadRequest, message = "User not found")
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = publications)
        }
    }
}