package com.example.routes

import com.example.data.models.Publication
import com.example.data.publicationservice.PublicationService
import com.example.data.requests.PublicationRequest
import com.example.data.responses.PublicationResponse
import com.example.features.save
import com.example.features.toFile
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random

fun Route.getPublicationRoutes(publicationService: PublicationService) {
    authenticate {
        get("publications") {
            val category = call.parameters["category"]
            val id = call.parameters["id"]

            if (category == null && id == null) {
                val publications = publicationService.getAllPublications()
                call.respond(status = HttpStatusCode.OK, message = publications)
            }

            if (category != null && id != null) {
                val publication = publicationService.getPublicationById(category = category, publicationId = id)
                if (publication != null) call.respond(status = HttpStatusCode.OK, message = publication)
                else call.respond(status = HttpStatusCode.BadRequest, message = "Publication doesn't exist")
            }

            if (category != null && id == null) {
                val publications = publicationService.getPublicationsByCategory(category)
                call.respond(status = HttpStatusCode.OK, message = publications)
            }

            call.respond(status = HttpStatusCode.BadRequest, message = "Invalid request")
        }
    }
}

fun Route.postPublicationRoutes(publicationService: PublicationService) {
    authenticate {
        post("publications/new") {
            val multipart = call.receiveMultipart()
            val parts = mutableListOf<PartData.FileItem>()
            var publicationRequest: PublicationRequest? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> parts.add(part)
                    is PartData.FormItem -> {
                        if (part.name == "publicationData") {
                            val jsonString = part.value
                            publicationRequest = Json.decodeFromString<PublicationRequest>(jsonString)
                        }
                    }
                    else -> Unit
                }
                part.dispose
            }

            val publication = Publication(
                imageUrl = "example image url",
                title = publicationRequest?.title ?: "",
                description = publicationRequest?.description ?: "",
                price = publicationRequest?.price ?: "",
                priceType = publicationRequest?.priceType ?: "",
                district = publicationRequest?.district ?: "",
                timeStamp = publicationRequest?.timeStamp ?: "",
                category = publicationRequest?.category ?: "",
                userId = publicationRequest?.userId ?: "",
                socials = publicationRequest?.socials ?: ""
            )

            val publicationUploaded = publicationService.insertPublication(publication)

            if (!publicationUploaded) {
                call.respond(status = HttpStatusCode.BadRequest, message = PublicationResponse(success = false))
                return@post
            }

            val files: List<File> = parts.mapNotNull { part ->
                val random = Random.nextInt(1000, 10000)
                val fileName = "image_$random"
                if (part.name == "images") part.toFile(fileName, ".jpeg")
                else null
            }
            val results = mutableListOf<Boolean>()

            files.forEach { file ->
                file.save("build/resources/main/static/images/", file.name)
                results.add(publicationService.insertFile(file, file.name, publication))
            }

            if (results.contains(false)) {
                call.respond(status = HttpStatusCode.BadRequest, message = PublicationResponse(success = false))
                return@post
            }

            call.respond(status = HttpStatusCode.OK, message = PublicationResponse(success = true))
        }
    }
}