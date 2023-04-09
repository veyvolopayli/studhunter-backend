package com.example.data.publicationservice

import com.example.data.models.Publication
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface PublicationService {
    suspend fun getAllPublications(): List<Publication>

    suspend fun getPublicationsByCategory(category: String): List<Publication>

    suspend fun getPublicationById(category: String, publicationId: String): Publication?

    suspend fun insertPublication(publication: Publication): Boolean

    suspend fun insertFile(file: File, fileName: String, publication: Publication): Boolean
}