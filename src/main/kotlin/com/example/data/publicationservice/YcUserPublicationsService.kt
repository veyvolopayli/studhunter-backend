package com.example.data.publicationservice

import com.amazonaws.services.s3.AmazonS3

class YcUserPublicationsService(val s3: AmazonS3) : UserPublicationsService {
    override suspend fun addNewPub() {
        
    }

    override suspend fun deletePub() {

    }

}