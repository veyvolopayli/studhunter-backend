package com.studhunter.api.vacancy.routing

import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.vacancy.dto.CreateVacancyRequest
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
    }
}