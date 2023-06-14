package com.example.routes

import com.amazonaws.services.s3.AmazonS3
import com.example.BUCKET_NAME
import com.example.data.constants.Constants
import com.example.data.models.Publication
import com.example.data.publicationservice.PublicationService
import com.example.data.requests.ApprovePublicationRequest
import com.example.data.requests.FavoritePubRequest
import com.example.data.requests.PublicationRequest
import com.example.data.responses.PublicationResponse
import com.example.features.toFile
import com.example.postgresdatabase.common.Categories
import com.example.postgresdatabase.publicationinteractions.PublicationViews
import com.example.postgresdatabase.publications.FavoritePublications
import com.example.postgresdatabase.publications.Publications
import com.example.postgresdatabase.users.Users
import com.example.repositories.PublicationRepository
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

fun Route.getPublicationRoutes() {
    authenticate {
        get("publications/id/{id}") {
            val id = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val publication = Publications.getPublication(id = id) ?: kotlin.run {
                call.respond(status = HttpStatusCode.BadRequest, message = "Publication does not exist")
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = publication)

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@get
            }

            val user = Users.fetchUserById(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@get
            }

            PublicationViews.insertView(id, user.username)

        }

        get("publications/favorites/fetch") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@get
            }

            val favorites = FavoritePublications.fetchFavorites(userId)

            call.respond(status = HttpStatusCode.OK, message = favorites)
        }
    }

    get("publications/query/{query}") {
        val query = call.parameters["query"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val publications = Publications.getPublicationsByQuery(query = query)
        call.respond(status = HttpStatusCode.OK, message = publications ?: emptyList())
    }

    get("publications/category/{category}") {
        val category = call.parameters["category"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val publications = Publications.getPublicationsByCategory(category = category)
        call.respond(status = HttpStatusCode.OK, message = publications ?: emptyList())

    }

    get("publications/fetch") {
        val publications = Publications.getAllPublications()
        call.respond(status = HttpStatusCode.OK, message = publications ?: emptyList())
    }

    get("publications/views/{id}") {
        val publicationId = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        val viewCount = PublicationViews.fetchViewCount(publicationId)

        call.respond(status = HttpStatusCode.OK, message = viewCount)
    }

    get("publications/favorites/count/{id}") {
        val publicationId = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        val count = FavoritePublications.fetchPubInFavoritesCount(publicationId)

        call.respond(status = HttpStatusCode.OK, message = count)
    }

    get("publication/categories") {
        val categories = Categories.getCategories() ?: run {
            call.respond(status = HttpStatusCode.Conflict, "Some categories database error")
            return@get
        }

        call.respond(status = HttpStatusCode.OK, message = categories)
    }

}

fun Route.postPublicationRoutes(publicationService: PublicationService) {
    authenticate {
        post("publications/new") {
            val multipart = call.receiveMultipart()
            val imageParts = mutableListOf<PartData.FileItem>()
            var publicationRequest: PublicationRequest? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        imageParts.add(part)
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

            if (imageParts.isEmpty()) {
                call.respond(status = HttpStatusCode.BadRequest, message = "Bad request")
                return@post
            }

            publicationRequest?.let { request ->

                val price = if (request.priceType !in 0..1) null else request.price

                val priceType = request.priceType.toPriceType() ?: run {
                    call.respond(status = HttpStatusCode.BadRequest, "Provided price type doesn't exist")
                    return@post
                }

                val publication = Publication(
                    title = request.title,
                    description = request.description,
                    price = price,
                    priceType = priceType,
                    district = request.district,
                    category = request.category,
                    userId = request.userId,
                    socials = request.socials
                )

                val createdPublicationId = Publications.insertPublication(publication) ?: run {
                    call.respond(status = HttpStatusCode.Conflict, message = "Some error with database")
                    return@post
                }

                val images: List<File> = imageParts.mapIndexedNotNull { index, fileItem ->
                    val fileName = "image_$index"
                    if (fileItem.name == "images") fileItem.toFile(fileName, ".jpeg")
                    else null
                }

                images.forEachIndexed { index, file ->

                    val fileInserted = publicationService.insertPublicationImage(
                        file = file,
                        fileName = "image_$index",
                        pubId = createdPublicationId
                    )

                    if (!fileInserted) {
                        call.respond(status = HttpStatusCode.Conflict, message = "Failed to upload images")
                        return@post
                    }
                }

                call.respond(status = HttpStatusCode.OK, message = PublicationResponse(success = true))
                return@post
            }

            call.respond(status = HttpStatusCode.BadRequest, message = "Bad request")

        }

        post("publications/favorites/add") {
            val request = call.receiveNullable<FavoritePubRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            FavoritePublications.insertFavorite(userId, request.publicationId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }

        post("publications/favorites/remove-single") {
            val request = call.receiveNullable<FavoritePubRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            FavoritePublications.removeFavorite(userId, request.publicationId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }

        post("publications/favorites/remove-all") {

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            FavoritePublications.removeAllUserFavorites(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.publicationOperationRoutes() {
    authenticate {
        post("publications/moderate") {
            val request = call.receiveNullable<ApprovePublicationRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

//            val token = call.request.headers["Authorization"]

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val username = Users.fetchUserById(userId)?.username ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            val isAdmin = username == "admin"

            println(username)

            if (!isAdmin) {
                call.respond(status = HttpStatusCode.Conflict, message = "You can't do that")
                return@post
            }

            val pubId = request.publicationId
            val approved = request.approved

            Publications.updatePublicationStatus(pubId, approved) ?: kotlin.run {
                call.respond(status = HttpStatusCode.InternalServerError, "Error")
                return@post
            }

            call.respond(status = HttpStatusCode.OK, message = "Success\nApproved: ${request.approved}")

        }
    }
}

fun Route.getUserPubs(publicationRepository: PublicationRepository) {
    authenticate {
        get("user/{id}/publications") {
            val userId = call.parameters["id"] ?: kotlin.run {
                call.respond(status = HttpStatusCode.BadRequest, message = "Invalid link")
                return@get
            }
            val publications = publicationRepository.getPublicationsByUserId(userId) ?: kotlin.run {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = "User doesn't have publications or doesn't exist"
                )
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = publications)
        }
    }
}

fun Route.imageRoutes(s3: AmazonS3) {
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

        val s3Object =
            s3.getObject(BUCKET_NAME, "${Constants.PUB_IMAGES}/$publicationId/$imageName.jpeg") ?: kotlin.run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Image not found"
                )
                return@get
            }

        val inputStream = s3Object.objectContent
        val bytes = inputStream.readBytes()

        call.respondBytes(bytes, ContentType.Image.JPEG)
    }
}

private fun Int.toPriceType() = Constants.priceTypes[this]