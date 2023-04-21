package com.example.data.publicationservice

interface UserPublicationsService {
    suspend fun addNewPub()

    suspend fun deletePub()
}