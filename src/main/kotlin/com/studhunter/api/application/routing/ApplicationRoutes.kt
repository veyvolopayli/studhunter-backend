package com.studhunter.api.application.routing

import com.studhunter.api.application.model.CreateApplicationRequest
import com.studhunter.api.application.model.UpdateApplicationStatusRequest
import com.studhunter.api.application.tables.Applications
import com.studhunter.api.chat.tables.Chats
import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.vacancy.tables.Vacancies
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.applicationRoutes() {
    authenticate {
        post("/applications/new") {
            val request = call.receiveNullable<CreateApplicationRequest>() ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val studentId = call.getAuthenticatedUserID() ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            Vacancies.getVacancy(request.vacancyId) ?: run {
                call.respond(HttpStatusCode.NotFound, "Вакансия не найдена")
                return@post
            }
            val duplicates = Applications.getByStudent(studentId)
                .any { it.vacancyId.toString() == request.vacancyId }
            if (duplicates) {
                call.respond(HttpStatusCode.Conflict, "Отклик уже существует");return@post
            }
            val id = Applications.insert(request, UUID.fromString(studentId)) ?: run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            Chats.getOrCreate(
                studentId = studentId,
                vacancyId = request.vacancyId
            )

            call.respond(HttpStatusCode.Created, id.toString())
        }

        get("/applications/my") {
            val studentId = call.getAuthenticatedUserID() ?: run {
                call.respond(HttpStatusCode.Unauthorized);return@get
            }
            call.respond(HttpStatusCode.OK, Applications.getByStudent(studentId))
        }

        get("/vacancies/{id}/applications") {
            val vacancyId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest);return@get
            }
            val userId = call.getAuthenticatedUserID() ?: run {
                call.respond(HttpStatusCode.Unauthorized);return@get
            }
            val vacancy = Vacancies.getVacancy(vacancyId) ?: run {
                call.respond(HttpStatusCode.NotFound);return@get
            }
            if (vacancy.employerId.toString() != userId) {
                call.respond(HttpStatusCode.Forbidden);return@get
            }
            call.respond(HttpStatusCode.OK, Applications.getByVacancy(vacancyId))
        }

        put("/applications/{id}/status") {
            val appId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest);return@put
            }
            val req = call.receiveNullable<UpdateApplicationStatusRequest>() ?: run {
                call.respond(HttpStatusCode.BadRequest);return@put
            }
            val application = Applications.get(appId) ?: run {
                call.respond(HttpStatusCode.NotFound);return@put
            }
            val vacancy = Vacancies.getVacancy(application.vacancyId.toString()) ?: run {
                call.respond(HttpStatusCode.NotFound);return@put
            }
            val employerId = call.getAuthenticatedUserID() ?: run {
                call.respond(HttpStatusCode.Unauthorized);return@put
            }
            if (vacancy.employerId.toString() != employerId) {
                call.respond(HttpStatusCode.Forbidden);return@put
            }
            val ok = Applications.updateStatus(appId, req.status)
            call.respond(if (ok) HttpStatusCode.OK else HttpStatusCode.Conflict)
        }
    }
}
