package com.studhunter.api.updates.routes

import com.studhunter.api.updates.responses.CheckUpdateResponse
import com.studhunter.api.updates.repository.UpdateRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.updateRoutes(ycUpdateRepository: UpdateRepository) {
    get("update/check/{version}") {
        val version = call.parameters["version"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val updateVersionExists = ycUpdateRepository.isUpdateFileExist(version) ?: kotlin.run {
            call.respond(status = HttpStatusCode.Conflict, message = "Some cloud exception")
            return@get
        }

        call.respond(status = HttpStatusCode.OK, message = CheckUpdateResponse(exists = updateVersionExists))
    }

    get("update/download/{version}") {
        val version = call.parameters["version"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val update = ycUpdateRepository.getUpdateFile(version) ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        call.response.header("Content-Disposition", "attachment; filename=\"stud-hunter-$version.apk\"")

        call.respondBytes(update, ContentType.Application.OctetStream, HttpStatusCode.OK)
    }

    get("update/download/last") {
        val lastVersion = ycUpdateRepository.getLastVersionFile() ?: kotlin.run {
            call.respond(status = HttpStatusCode.Conflict, message = "Some cloud error")
            return@get
        }
        val byteArray = lastVersion.first
        val fileName = lastVersion.second

        call.response.header("Content-Disposition", "attachment; filename=\"$fileName\"")
        call.respondBytes(byteArray, ContentType.Application.OctetStream, HttpStatusCode.OK)
    }

}