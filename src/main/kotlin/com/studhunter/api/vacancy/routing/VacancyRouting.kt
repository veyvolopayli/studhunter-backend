package com.studhunter.api.vacancy.routing

import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.vacancy.dto.CreateVacancyRequest
import com.studhunter.api.vacancy.dto.UpdateVacancyRequest
import com.studhunter.api.vacancy.model.VacancyFilter
import com.studhunter.api.vacancy.tables.Vacancies
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.vacancyRoutes() {
    authenticate {
        post("/vacancies/new") {
            val request = call.receiveNullable<CreateVacancyRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val areFieldsBlank =
                request.title.isBlank() ||
                        request.category.isBlank() ||
                        request.description.isBlank()
            if (areFieldsBlank) {
                call.respond(status = HttpStatusCode.Conflict, message = "Не заполнены обязательные поля")
                return@post
            }

            val currentUserID = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Unauthorized, message = "Не авторизован")
                return@post
            }

            val newVacancyId = Vacancies.insertVacancy(UUID.fromString(currentUserID), request)

            call.respond(HttpStatusCode.Created, newVacancyId.toString())
        }

        get("/vacancies/{id}") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest);return@get
            }
            val vacancy = Vacancies.getVacancy(id) ?: run {
                call.respond(HttpStatusCode.NotFound);return@get
            }
            call.respond(HttpStatusCode.OK, vacancy)
        }

        put("/vacancies/{id}") {
            val idParam = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest);return@put
            }
            val employerId = call.getAuthenticatedUserID() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "Не авторизован");return@put
            }
            val vacancy = Vacancies.getVacancy(idParam) ?: run {
                call.respond(HttpStatusCode.NotFound);return@put
            }
            if (vacancy.employerId.toString() != employerId) {
                call.respond(HttpStatusCode.Forbidden, "Нет доступа");return@put
            }
            val req = call.receiveNullable<UpdateVacancyRequest>() ?: run {
                call.respond(HttpStatusCode.BadRequest);return@put
            }
            val ok = Vacancies.updateVacancy(idParam, req) ?: false
            call.respond(if (ok) HttpStatusCode.OK else HttpStatusCode.Conflict)
        }

        delete("/vacancies/{id}") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest);return@delete
            }
            val employerId = call.getAuthenticatedUserID() ?: run {
                call.respond(HttpStatusCode.Unauthorized);return@delete
            }
            val ok = Vacancies.deleteVacancy(id, employerId) ?: false
            call.respond(if (ok) HttpStatusCode.OK else HttpStatusCode.Conflict)
        }

        post("/vacancies/search") {
            val filter = call.receiveNullable<VacancyFilter>() ?: VacancyFilter()
            val list = Vacancies.search(filter) ?: run {
                call.respond(HttpStatusCode.InternalServerError);return@post
            }
            call.respond(HttpStatusCode.OK, list)
        }
    }
}