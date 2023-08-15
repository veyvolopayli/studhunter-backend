package com.studhunter.api.publications.repository

import java.io.File

interface YCloudPublicationsRepository {
    suspend fun insertPublicationImage(file: File, imageIndex: Int, pubId: String): Boolean
}