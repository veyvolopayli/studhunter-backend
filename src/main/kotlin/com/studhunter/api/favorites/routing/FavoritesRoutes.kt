package com.studhunter.api.favorites.routing

import com.studhunter.api.favorites.model.FavoritePublication
import com.studhunter.api.favorites.tables.FavoritePublications
import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.publications.requests.FavoritePubRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlin.collections.LinkedHashMap

fun Route.favoritePublicationRoutes() {
    val allFetchedFavorites = FavoritePublications.getAllFavorites() ?: emptyMap()
    val allFavorites = Collections.synchronizedMap<String, FavoritePublication>(LinkedHashMap()).also { it.putAll(allFetchedFavorites) }

    authenticate {
        post("favorites/publication/add") {
            val request = call.receiveNullable<FavoritePubRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userID = call.getAuthenticatedUserID() ?: kotlin.run {
                call.respond(status = HttpStatusCode.Conflict, message = "JWT exception")
                return@post
            }

            /*FavoritePublications.insertFavorite(userID, request.publicationId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }*/

            val key = request.publicationId.dropLast(18) + userID.dropLast(18)
            allFavorites[key] = FavoritePublication(userID = userID, favoritePubID = request.publicationId)

            call.respond(HttpStatusCode.OK)
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

            /*FavoritePublications.removeFavorite(userId, request.publicationId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }*/

            call.respond(HttpStatusCode.OK)
        }

        get("favorites/publication/{id}/check") {
            val userID = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Unauthorized, "token is not valid")
                return@get
            }
            val pubID = call.parameters["id"] ?: run {
                call.respond(status = HttpStatusCode.BadRequest, message = "Invalid id")
                return@get
            }
            /*val isFavorite = FavoritePublications.isInFavorites(userID, pubID) ?: run {
                call.respond(status = HttpStatusCode.InternalServerError, message = "HAHAHAAHAHHAHAHAH")
                return@get
            }*/

            val isFavorite = allFavorites.

            call.respond(status = HttpStatusCode.OK, message = isFavorite)
        }
    }
}