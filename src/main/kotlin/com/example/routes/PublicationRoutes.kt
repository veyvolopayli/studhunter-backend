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
import java.util.UUID
import kotlin.random.Random

fun Route.getPublicationRoutes(publicationService: PublicationService) {
    authenticate {
        get("publications") {
            val category = call.parameters["category"]
            val id = call.parameters["id"]

            println("publications/pubs/$category/$id/$id.json")

            if (category == null && id == null) {
                val publications = publicationService.getAllPublications()
                call.respond(status = HttpStatusCode.OK, message = publications)
                return@get
            }

            if (category != null && id != null) {
                val publication = publicationService.getPublicationById(category = category, publicationId = id)
                if (publication != null) call.respond(status = HttpStatusCode.OK, message = publication)
                else call.respond(status = HttpStatusCode.BadRequest, message = "Publication doesn't exist")
                return@get
            }

            if (category != null && id == null) {
                val publications = publicationService.getPublicationsByCategory(category)
                call.respond(status = HttpStatusCode.OK, message = publications)
                return@get
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

            val pubId = UUID.randomUUID().toString()
            val pubCategory = publicationRequest?.category ?: "Другое"

            val files: List<File> = parts.mapIndexedNotNull { index, fileItem ->
                val fileName = "image_$index"
                if (fileItem.name == "images") fileItem.toFile(fileName, ".jpeg")
                else null
            }
            val results = mutableListOf<Boolean>()

            files.forEachIndexed { index, file ->
//                file.save("build/resources/main/static/images/", file.name)
                results.add(
                    publicationService.insertFile(
                        file = file, fileName = "image_$index", category = pubCategory, pubId = pubId
                    )
                )
            }

            if (results.contains(false)) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Критическая ошибка при загрузке изображений"
                )
                return@post
            }

            val cardImage = files[0]

            val cardImageUrl = publicationService.generateTemporaryImageUrl(
                category = pubCategory,
                pubId = pubId,
                fileName = "image_0"
            )

            println("Дата:")
            println(cardImageUrl.expiresIn)

            val publication = Publication(
                id = pubId,
                imageUrl = cardImageUrl.url ?: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAMAAACahl6sAAAAOVBMVEXg4OB1dXXX19fd3d2EhIR9fX14eHjJycm2trbb29uurq6goKCZmZmIiIiBgYHNzc2np6e8vLySkpKXK8HrAAABuUlEQVR4nO3Z0bKCIBCAYQNFVCzr/R/2nHU6k8KpJi6wZf7vLu1id9gFhKYBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAb249h7pzr5jD29uhospnlfNo4L+boiLKYyZ0iblKYiu/iNER3PTquD9npPgbB98Za0/twH59JVasMtzXo1m+iHny7PrwpysSuebgxCtmOTlkma121l/TFZR2UqXxEebxEO/87QZlZ3inpeCPzVftkojUyJp2OWVgKy23qSsbg8evitBSXkUjHzYN9Is0oeWoYkkUKazsxRYlYKa6ldFSfs7K/8tsnUSLrXHAuG1SOXpp5t1LEiQxSe33ZqDJIC4TdkziRJkRN9J1CXFlpIj7J9RvNSd0kiUj1zSVjyiKr4X5yTRIx0kYlY8oinbzfFSaJWFlJSsaUpZpEqimttNkTOpo9nX4TOqbfdEFM6FgQpW7c8OofSrYo1Wwaq9nG1/NhVc2nbj2HD821kuOgeg7o3hyZBj1Hpo9D7M3K+HeIrSmPeq4Vfl3ruOhpnly9vdyEfa1KLkPF7nr66GAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPjcD13rCcC3ILx/AAAAAElFTkSuQmCC",
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

            call.respond(status = HttpStatusCode.OK, message = PublicationResponse(success = true))
        }
    }
}