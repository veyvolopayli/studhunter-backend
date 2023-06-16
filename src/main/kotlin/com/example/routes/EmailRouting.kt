package com.example.routes

import com.example.data.requests.ConfirmCodeRequest
import com.example.data.responses.UserStatusResponse
import com.example.postgresdatabase.users.UserData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.emailRouting() {
    post("users/user/email/confirm") {
        val request = call.receiveNullable<ConfirmCodeRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val userId = request.userId
        val code = request.code

        UserData.confirm(userId, code) ?: kotlin.run {
            call.respond(status = HttpStatusCode.Conflict, "Wrong confirmation code or email already confirmed")
            return@post
        }

        call.respond(status = HttpStatusCode.OK, message = "Success")
    }

    get("user/{id}/status") {

        val userId = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val userEmailConfirmed = UserData.fetchUserEmailConfirmed(userId) ?: kotlin.run {
            call.respond(status = HttpStatusCode.BadRequest, message = "User not found")
            return@get
        }

        call.respond(status = HttpStatusCode.OK, message = UserStatusResponse(userEmailConfirmed))
    }

}