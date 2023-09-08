package com.studhunter.api.users.routing

import com.amazonaws.services.s3.AmazonS3
import com.studhunter.BUCKET_NAME
import com.studhunter.api.common.tables.Universities
import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.publications.tables.PublicationViews
import com.studhunter.api.publications.tables.Publications
import com.studhunter.api.users.repository.UsersRepository
import com.studhunter.api.users.responses.UserResponse
import com.studhunter.api.users.responses.toShortUserResponse
import com.studhunter.api.favorites.tables.FavoritePublications
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.studhunter.api.common.Constants
import com.studhunter.api.features.toCompressedImageFile
import com.studhunter.api.users.requests.EditProfileRequest
import com.studhunter.api.users.tables.Users

fun Route.userRouting(userRepository: UsersRepository, s3: AmazonS3) {

    get("user/public/get") {
        makeGetUserCall(call, userRepository, false)
    }

    get("universities/get") {
        val universities = Universities.getUniversities() ?: run {
            call.respond(status = HttpStatusCode.InternalServerError, "Couldn't get universities")
            return@get
        }
        call.respond(status = HttpStatusCode.OK, message = universities)
    }

    authenticate {
        post("avatar/upload") {
            val uid = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Conflict, "JWT token exception")
                return@post
            }
            call.receiveMultipart().forEachPart { partData ->
                if (partData is PartData.FileItem && partData.name == "avatar") {
                    withContext(Dispatchers.IO) {
                        val stream = partData.streamProvider()
                        try {
                            val file = stream.toCompressedImageFile(0.5)
                            s3.putObject(BUCKET_NAME, Constants.AVATARS_PATH + uid, file)
                            if (file.exists()) file.delete()
                            call.respondText("Success")
                        } catch (e: Exception) {
                            call.respond(status = HttpStatusCode.Conflict, "Failed to upload image")
                            e.printStackTrace()
                        } finally {
                            stream.close()
                        }
                    }
                }
                partData.dispose()
            }
        }

        get("user/get") {
            makeGetUserCall(call, userRepository, true)
        }
        get("my-publications") {
            val userID = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "Something wrong with jwt")
                return@get
            }

            val userPublications = Publications.getPublicationsByUserId(userId = userID)?.map { publication ->
                val views = PublicationViews.fetchViewsCount(publicationId = publication.id)
                val favorites = FavoritePublications.fetchPubInFavoritesCount(publicationId = publication.id)
                publication.toMyPublication(views = views, favorites = favorites)
            } ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "User doesn't have publications or doesn't exist")
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = userPublications)
        }

        post("profile/edit") {
            val editProfileRequest = call.receiveNullable<EditProfileRequest>() ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userID = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.BadRequest, message = "JWT token required")
                return@post
            }
            val edit = Users.editUser(userID, editProfileRequest) ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "DB error")
                return@post
            }
            call.respond(status = HttpStatusCode.OK, message = edit)
        }
    }

    get("user/{id}/avatar") {
        val uid = call.parameters["id"] ?: run {
            call.respond(status = HttpStatusCode.BadRequest, "token required")
            return@get
        }
        try {
            val profileImage = s3.getObject(BUCKET_NAME, Constants.AVATARS_PATH + uid).objectContent.readBytes()
            call.respondBytes(profileImage, ContentType.Image.JPEG)
        } catch (e: Exception) {
            call.respond(status = HttpStatusCode.BadRequest, "Profile image not found")
        }
    }

    get("user/{id}/publications") {
        val userID = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val userPublications = Publications.getPublicationsByUserId(userId = userID) ?: run {
            call.respond(status = HttpStatusCode.Conflict, message = "User doesn't have publications or doesn't exist")
            return@get
        }

        call.respond(status = HttpStatusCode.OK, message = userPublications)
    }
}

private suspend fun makeGetUserCall(call: ApplicationCall, userRepository: UsersRepository, isAuthenticated: Boolean) {
    val queryParameters = call.request.queryParameters
    if (queryParameters.isEmpty()) {
        call.respond(HttpStatusCode.BadRequest)
        return
    }
    val userId = queryParameters["id"]
    val userEmail = queryParameters["email"]
    val username = queryParameters["username"]

    var user: UserResponse? = null

    userId?.let { id ->
        user = userRepository.getUserById(id)
    }

    userEmail?.let { email ->
        user = userRepository.getUserByEmail(email)
    }

    username?.let { uname ->
        user = userRepository.getUserByUsername(uname)
    }

    user?.let {
        if (isAuthenticated) {
            call.respond(status = HttpStatusCode.OK, message = it)
        } else {
            call.respond(status = HttpStatusCode.OK, message = it.toShortUserResponse())
        }
        return
    }

    call.respond(status = HttpStatusCode.BadRequest, message = "User not found")
}