package com.example.data.publicationservice

import java.io.File

interface PublicationService {

    suspend fun insertPublicationImage(file: File, fileName: String, pubId: String): Boolean

//    suspend fun startPublicationsTask()

//    suspend fun updatePublications(): Boolean

}