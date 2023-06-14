package com.example.repositories

import com.example.data.models.Publication

interface PublicationRepository {
    fun insertPublication(publication: Publication): String?
    fun getPublication(id: String): Publication?
    fun getAllPublications(): List<Publication>?
    fun getPublicationsByQuery(query: String): List<Publication>?
    fun getPublicationsByCategory(category: String): List<Publication>?
    fun getPublicationsByUserId(userId: String): List<Publication>?
    fun getPublicationsByDistrict(district: String): List<Publication>?
    fun getClosestPublications(district: String): List<Publication>?
    fun updatePublicationStatus(pubId: String, approve: Boolean): Boolean?
}