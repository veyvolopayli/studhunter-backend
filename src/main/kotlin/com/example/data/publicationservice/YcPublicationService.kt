package com.example.data.publicationservice

import com.amazonaws.services.s3.AmazonS3
import com.example.BUCKET_NAME
import com.example.data.constants.Constants
import java.io.File

class YcPublicationService(private val s3: AmazonS3): PublicationService {

    override suspend fun insertPublicationImage(file: File, fileName: String, pubId: String): Boolean {
        val result = s3.putObject(BUCKET_NAME, "${Constants.PUB_IMAGES}/$pubId/$fileName.jpeg", file)
        return result != null
    }

}