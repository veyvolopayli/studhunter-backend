package com.studhunter.api.publications.routing

import com.amazonaws.services.s3.AmazonS3
import com.studhunter.BUCKET_NAME
import com.studhunter.api.common.Constants
import com.studhunter.api.common.tables.Categories
import com.studhunter.api.common.tables.Districts
import com.studhunter.api.common.tables.PriceTypes
import com.studhunter.api.features.deleteFile
import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.features.toCompressedImageFile
import com.studhunter.api.publications.model.DetailedPublication
import com.studhunter.api.publications.model.Publication
import com.studhunter.api.publications.repository.YCloudPublicationsRepository
import com.studhunter.api.publications.requests.ApprovePublicationRequest
import com.studhunter.api.publications.requests.FavoritePubRequest
import com.studhunter.api.publications.requests.PublicationRequest
import com.studhunter.api.publications.tables.PublicationViews
import com.studhunter.api.publications.tables.Publications
import com.studhunter.api.favorites.tables.FavoritePublications
import com.studhunter.api.users.tables.Users
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

            val currentUserID = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "Invalid JWT")
                return@get
            }

            val publication = Publications.getPublication(id = id) ?: kotlin.run {
                call.respond(status = HttpStatusCode.BadRequest, message = "Publication does not exist")
                return@get
            }

            val user = Users.getUserById(publication.userId) ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "User that have created publication is deleted from the platform")
                Publications.deleteUserPublications(publication.userId)
                return@get
            }

            val detailedPublication = DetailedPublication(
                publication = publication,
                user = user,
                userIsOwner = currentUserID == user.id
            )

            call.respond(status = HttpStatusCode.OK, message = detailedPublication)

            val userID = call.getAuthenticatedUserID() ?: return@get
            PublicationViews.insertView(id, userID)
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

        get("publications/{id}/delete") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            Publications.deletePublication(id) ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "Failed to delete publication")
                return@get
            }
        }
    }

    get("publications/query/{query}") {
        val query = call.parameters["query"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val publications = Publications.getPublicationsByQuery(query = query) ?: run {
            call.respond(status = HttpStatusCode.Conflict, "Something with publications database")
            return@get
        }
        call.respond(status = HttpStatusCode.OK, message = publications)
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

    get("publications/{id}/views") {
        val publicationId = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        val viewsCount = PublicationViews.fetchViewsCount(publicationId)

        call.respond(status = HttpStatusCode.OK, message = viewsCount)
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

    get("publication/priceTypes") {
        val priceTypes = PriceTypes.getPriceTypes() ?: run {
            call.respond(status = HttpStatusCode.Conflict, "Some price types database error")
            return@get
        }
        call.respond(status = HttpStatusCode.OK, message = priceTypes)
    }

    get("publication/districts") {
        val districts = Districts.getDistricts() ?: run {
            call.respond(status = HttpStatusCode.Conflict, "Obtaining districts ended in failure")
            return@get
        }
        call.respond(status = HttpStatusCode.OK, message = districts)
    }



}

fun Route.postPublicationRoutes(yCloudPublicationsRepository: YCloudPublicationsRepository) {
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
                call.respond(status = HttpStatusCode.BadRequest, message = "NO IMAGES")
                return@post
            }

            publicationRequest?.let { request ->

                if (request.priceType.trim() !in Constants.priceTypes) {
                    call.respond(status = HttpStatusCode.BadRequest, message = "Incorrect type of price")
                    return@post
                }

                val price = if (request.priceType !in Constants.priceTypes.subList(0, 2)) null else request.price

                val publication = Publication(
                    title = request.title,
                    description = request.description,
                    price = price,
                    priceType = request.priceType,
                    district = request.district,
                    category = request.category,
                    userId = request.userId,
                    socials = request.socials
                )

                val createdPublicationId = Publications.insertPublication(publication) ?: run {
                    call.respond(status = HttpStatusCode.Conflict, message = "Some error with database")
                    return@post
                }

                val images: List<File> = imageParts.mapNotNull { fileItem ->
                    if (fileItem.name == "images") {
                        val stream = fileItem.streamProvider()
                        val file = try {
                            stream.toCompressedImageFile(0.5)
                        } catch (e: Exception) {
                            null
                        }
                        file
                    }
                    else null
                }

                images.forEachIndexed { index, file ->

                    val fileInserted = yCloudPublicationsRepository.insertPublicationImage(
                        file = file,
                        imageIndex = index,
                        pubId = createdPublicationId
                    )

                    file.deleteFile() ?: run {
                        call.respond(status = HttpStatusCode.Conflict, message = "Failed to delete a temp image")
                        return@post
                    }

                    if (!fileInserted) {
                        call.respond(status = HttpStatusCode.Conflict, message = "Failed to upload images")
                        Publications.deletePublication(createdPublicationId)
                        return@post
                    }
                }

                call.respond(status = HttpStatusCode.OK, message = createdPublicationId)
                return@post
            }

            call.respond(status = HttpStatusCode.BadRequest, message = "Bad request")

        }

        /*post("favorites/publication/add") {
            val request = call.receiveNullable<FavoritePubRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userID = call.getAuthenticatedUserID() ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            FavoritePublications.insertFavorite(userID, request.publicationId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }*/

        /*get("favorites/publication/{id}/check") {
            val userID = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Unauthorized, "token is not valid")
                return@get
            }
            val pubID = call.parameters["id"] ?: run {
                call.respond(status = HttpStatusCode.BadRequest, message = "Invalid id")
                return@get
            }
            val isFavorite = FavoritePublications.isInFavorites(userID, pubID) ?: run {
                call.respond(status = HttpStatusCode.InternalServerError, message = "HAHAHAAHAHHAHAHAH")
                return@get
            }
            call.respond(status = HttpStatusCode.OK, message = isFavorite)
        }

        post("favorites/publication/remove-single") {
            val request = call.receiveNullable<FavoritePubRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userId = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Unauthorized, "token is not valid")
                return@post
            }

            FavoritePublications.removeFavorite(userId, request.publicationId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }*/

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

            val username = Users.getUserById(userId)?.username ?: kotlin.run {
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
            call.respond(status = HttpStatusCode.BadRequest, message = "Invalid link")
            return@get
        }
        val s3Object = s3.getObject(BUCKET_NAME, "${Constants.PUB_IMAGES}/$publicationId/$imageName.jpeg") ?: run {
            call.respond(status = HttpStatusCode.BadRequest, message = "Image not found")
            return@get
        }
        val bytes = s3Object.objectContent.readBytes()

        call.respondBytes(bytes, ContentType.Image.JPEG)
    }
}