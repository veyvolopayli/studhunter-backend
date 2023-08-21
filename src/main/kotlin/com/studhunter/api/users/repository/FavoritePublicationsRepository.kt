package com.studhunter.api.users.repository

import com.studhunter.api.publications.model.Publication

interface FavoritePublicationsRepository {
    fun fetchFavorites(uid: String): List<Publication>
    fun isInFavorites(userID: String, pubID: String): Boolean?
    fun fetchPubInFavoritesCount(publicationId: String): Long
    fun insertFavorite(uid: String, publicationId: String): Boolean?
    fun removeFavorite(uid: String, publicationId: String): Boolean?
    fun removeAllUserFavorites(uid: String): Boolean?
}