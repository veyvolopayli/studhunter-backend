package com.studhunter.api.favorites.routing

import com.studhunter.api.common.convertHoursToMillis
import com.studhunter.api.common.startTask
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.LinkedHashMap

fun Route.favoritePublicationRoutes() {
    val allFetchedFavorites = FavoritePublications.getAllFavorites() ?: emptySet()
    val allFavorites = Collections.synchronizedSet<FavoritePublication>(LinkedHashSet()).also { it.addAll(allFetchedFavorites) }

    CoroutineScope(Dispatchers.Default).launch {
        startTask(convertHoursToMillis(0.5.toLong())) {
            FavoritePublications.synchronizeDatabaseWithList(allFavorites) ?: println("Ошибка")
        }
    }

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

            val isSuccessful = allFavorites.add(FavoritePublication(userID = userID, favoritePubID = request.publicationId))

            call.respond(status = HttpStatusCode.OK, message = isSuccessful)
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

            val isSuccessful = allFavorites.remove(FavoritePublication(userID = userId, favoritePubID = request.publicationId))

            call.respond(status = HttpStatusCode.OK, message = isSuccessful)
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

            val isFavorite = allFavorites.contains(FavoritePublication(userID, pubID))

            call.respond(status = HttpStatusCode.OK, message = isFavorite)
        }
    }
}