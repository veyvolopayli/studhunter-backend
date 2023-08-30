package com.studhunter.api.publications.yandex_cloud

import com.amazonaws.services.s3.AmazonS3
import com.studhunter.BUCKET_NAME
import com.studhunter.api.publications.repository.YCloudPublicationsRepository
import java.io.File

class YCloudPublicationRepositoryImpl(private val s3: AmazonS3) : YCloudPublicationsRepository {

    private val imagePathFor: (String, Int) -> String = { pubID, imageIndex ->
        "publications/images/$pubID/$imageIndex"
    }
    override suspend fun insertPublicationImage(file: File, imageIndex: Int, pubId: String): Boolean {
        return try {
            s3.putObject(BUCKET_NAME, imagePathFor(pubId, imageIndex), file)
            true
        } catch (e: Exception) {
            false
        }
    }
}