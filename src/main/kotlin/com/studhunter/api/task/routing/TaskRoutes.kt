package com.studhunter.api.task.routing

import com.studhunter.api.chat.tables.Tasks
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRoutes() {

    authenticate {
        get("tasks") {
            val userId = call.parameters["userId"]
            val userStatus = call.parameters["userStatus"]
            val taskStatus = call.parameters["taskStatus"]

            if (userId == null || userStatus == null || taskStatus == null) {
                call.respond(status = HttpStatusCode.BadRequest, message = "Not enough information for the request")
                return@get
            }

            val tasks = Tasks.getTasks(userId = userId, userStatus = userStatus, taskStatus = taskStatus) ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "Wrong request")
                return@get
            }

            call.respond(tasks)
        }
    }
}