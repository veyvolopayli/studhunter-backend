package com.example.routes

import com.example.data.constants.HOST
import com.example.data.constants.NEW_PUB_IMAGES_PATH
import com.example.data.constants.SERVER_NEW_PUB_IMAGES_PATH
import com.example.data.models.Publication
import com.example.data.publicationservice.PublicationService
import com.example.data.requests.PublicationRequest
import com.example.data.responses.PublicationResponse
import com.example.features.save
import com.example.features.toFile
import com.example.postgresdatabase.publications.Publications
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
import java.util.UUID

fun Route.getPublicationRoutes() {
    authenticate {
        get("publications") {
            val query = call.parameters["query"]
            val category = call.parameters["category"]
            val id = call.parameters["id"]

            if (query != null) {
                val publications = Publications.fetchPublicationsByQuery(query = query)
                call.respond(status = HttpStatusCode.OK, message = publications ?: emptyList())
                return@get
            }

            if (category != null) {
                val publications = Publications.fetchPublicationsByCategory(searchCategory = category)
                call.respond(status = HttpStatusCode.OK, message = publications ?: emptyList())
                return@get
            }

            if (id != null) {
                val publication = Publications.fetchPublication(searchId = id) ?: kotlin.run {
                    call.respond(status = HttpStatusCode.BadRequest, message = "Publication does not exist")
                    return@get
                }
                call.respond(status = HttpStatusCode.OK, message = publication)
                return@get
            }

        }

        get("publications/fetch") {
            val publications = Publications.fetchAllPublications()
            call.respond(status = HttpStatusCode.OK, message = publications ?: emptyList())
            return@get
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
                    is PartData.FileItem -> {
                        parts.add(part)
                    }

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

            if (publicationRequest == null) {
                call.respond(status = HttpStatusCode.BadRequest, message = "Bad request")
                return@post
            }

            val pubId = UUID.randomUUID().toString()

            val files: List<File> = parts.mapIndexedNotNull { index, fileItem ->
                val fileName = "image_$index"
                if (fileItem.name == "images") fileItem.toFile(fileName, ".jpeg")
                else null
            }

            val pathToCardImage = "$HOST/image/$pubId/image_0"

            files.forEachIndexed { index, file ->
                val fileInserted = publicationService.insertPublicationImage(
                    file = file,
                    fileName = "image_$index",
                    pubId = pubId
                )
                if (!fileInserted) {
                    call.respond(status = HttpStatusCode.Conflict, message = "Failed to insert file to database")
                    return@post
                }
                file.save("$SERVER_NEW_PUB_IMAGES_PATH/$pubId", "/image_$index")
            }

            val publication = Publication(
                id = pubId,
                imageUrl = pathToCardImage,
                title = publicationRequest!!.title,
                description = publicationRequest!!.description,
                price = publicationRequest?.price ?: 0,
                priceType = publicationRequest!!.priceType,
                district = publicationRequest!!.district,
                category = publicationRequest!!.category,
                userId = publicationRequest!!.userId,
                socials = publicationRequest!!.socials
            )

            val publicationUploaded = publicationService.insertPublication(publication)

            if (!publicationUploaded) {
                call.respond(status = HttpStatusCode.BadRequest, message = PublicationResponse(success = false))
                return@post
            }

            Publications.insertPublication(publication)

            call.respond(status = HttpStatusCode.OK, message = PublicationResponse(success = true))

            publicationService.updatePublications()

        }
    }
}

fun Route.getUserPubs(publicationService: PublicationService) {
    authenticate {
        get("publications/user/{id}") {
            val userId = call.parameters["id"] ?: kotlin.run {
                call.respond(status = HttpStatusCode.BadRequest, message = "Invalid link")
                return@get
            }
            val userPubIds = publicationService.getUserPubIds(userId) ?: kotlin.run {
                call.respond(status = HttpStatusCode.BadRequest, message = "User doesn't have publications or doesn't exist")
                return@get
            }
            val ids = userPubIds.ids

            val publications = ids.mapNotNull {
                publicationService.getPublicationById(category = it.value, publicationId = it.key)
            }
            call.respond(status = HttpStatusCode.OK, message = publications)
        }
    }
}

fun Route.imageRoutes() {
    get("image/{id}/{name}") {
        val publicationId = call.parameters["id"] ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Invalid link"
            )
            return@get
        }
        val imageName = call.parameters["name"] ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Invalid link"
            )
            return@get
        }
        val imageFile = File("$SERVER_NEW_PUB_IMAGES_PATH/$publicationId/$imageName.jpeg")
        if (!imageFile.exists()) return@get call.respond(status = HttpStatusCode.NotFound, message = "Image not found")
        call.respondFile(imageFile)
    }
}