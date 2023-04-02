package com.example.data.publicationservice

import com.example.data.models.Publication
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface PublicationService {
    suspend fun getAllPublications(): ArrayList<Publication>

    suspend fun getPublicationByCategory(category: String): List<Publication>

    suspend fun getPublicationById(publicationId: String): Publication?

    suspend fun insertPublication(publication: Publication): Boolean

    suspend fun insertFile(file: File, fileName: String, publication: Publication): Boolean
}