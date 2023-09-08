package com.studhunter.api.favorites.model

import io.ktor.util.date.*

data class FavoritePublication(
    val userID: String,
    val favoritePubID: String,
    val timestamp: Long = getTimeMillis()
)
