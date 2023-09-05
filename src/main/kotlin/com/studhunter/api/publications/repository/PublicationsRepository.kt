package com.studhunter.api.publications.repository

import com.studhunter.api.publications.model.Publication

interface PublicationsRepository {
    fun insertPublication(publication: Publication): String?
    fun getPublication(id: String): Publication?
    fun getAllPublications(): List<Publication>?
    fun getPublicationsByQuery(query: String): List<Publication>?
    fun getPublicationsByCategory(category: String): List<Publication>?
    fun getPublicationsByUserId(userId: String): List<Publication>?
    fun getPublicationsByDistrict(district: String): List<Publication>?
    fun getClosestPublications(district: String): List<Publication>?
    fun updatePublicationStatus(pubId: String, approve: Boolean): Boolean?
    fun deletePublication(publicationId: String): Int?
    fun deleteUserPublications(userID: String): Int?
}