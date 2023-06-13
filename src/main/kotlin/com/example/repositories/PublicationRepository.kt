package com.example.repositories

import com.example.data.models.Publication

interface PublicationRepository {
    suspend fun insertPublication(publication: Publication): String?
    suspend fun getPublication(id: String): Publication
    suspend fun getAllPublications(): List<Publication>
    suspend fun getPublicationsByQuery(query: String): List<Publication>
    suspend fun getPublicationsByCategory(category: String): List<Publication>
    suspend fun getPublicationsByUserId(userId: String): List<Publication>
    suspend fun getPublicationsByDistrict(district: String): List<Publication>
    suspend fun getClosestPublications(district: String): List<Publication>
}