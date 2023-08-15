package com.studhunter.api.users.routing

import com.studhunter.api.users.responses.UserResponse
import com.studhunter.api.users.responses.toShortUserResponse
import com.studhunter.api.publications.tables.Publications
import com.studhunter.api.common.tables.Universities
import com.studhunter.api.users.repository.UsersRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRouting(userRepository: UsersRepository) {

    get("user/public/get") {
        makeGetUserCall(call, userRepository, false)
    }

    get("universities/get") {
        val universities = Universities.getUniversities() ?: run {
            call.respond(status = HttpStatusCode.InternalServerError, "Couldn't get universities")
            return@get
        }
        call.respond(status = HttpStatusCode.OK, message = universities)
    }

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

        get("user/get") {
            makeGetUserCall(call, userRepository, true)
        }

        get("user/publications/{id}") {
            val userId = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val publications = Publications.getPublicationsByUserId(userId = userId) ?: kotlin.run {
                call.respond(status = HttpStatusCode.BadRequest, message = "User not found")
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = publications)
        }
    }
}

private suspend fun makeGetUserCall(call: ApplicationCall, userRepository: UsersRepository, isAuthenticated: Boolean) {
    val queryParameters = call.request.queryParameters
    if (queryParameters.isEmpty()) {
        call.respond(HttpStatusCode.BadRequest)
        return
    }
    val userId = queryParameters["id"]
    val userEmail = queryParameters["email"]
    val username = queryParameters["username"]

    var user: UserResponse? = null

    userId?.let { id ->
        user = userRepository.getUserById(id)
    }

    userEmail?.let { email ->
        user = userRepository.getUserByEmail(email)
    }

    username?.let { uname ->
        user = userRepository.getUserByUsername(uname)
    }

    user?.let {
        if (isAuthenticated) {
            call.respond(status = HttpStatusCode.OK, message = it)
        } else {
            call.respond(status = HttpStatusCode.OK, message = it.toShortUserResponse())
        }
        return
    }

    call.respond(status = HttpStatusCode.BadRequest, message = "User not found")
}