package com.studhunter.api.task.routing

import com.studhunter.api.chat.model.Task
import com.studhunter.api.chat.tables.Tasks
import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.publications.tables.Publications
import com.studhunter.api.task.model.WideTask
import com.studhunter.api.users.tables.Users
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

            if (taskStatus !in listOf("accepted", "declined", "complete", "closed")) {
                call.respond(status = HttpStatusCode.BadRequest, message = "Provided taskStatus is not correct")
                return@get
            }

            /*val currentUserId = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "Wrong JWT")
                return@get
            }*/

            val tasks = Tasks.getTasks(userId = userId, userStatus = userStatus, taskStatus = taskStatus) ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "Wrong request")
                return@get
            }

            try {
                val wideTasks = tasks.map { task ->
                    WideTask(
                        task = task,
                        executor = Users.getUserById(task.executorId) ?: throw Exception("Executor of task doesn't exist"),
                        publication = Publications.getPublication(task.publicationId) ?: throw Exception("Publication doesn't exist")
                    )
                }
                call.respond(wideTasks)
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.Conflict, message = e.message ?: "Ooops")
            }

        }
    }
}