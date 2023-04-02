package com.example.data.publicationservice

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.example.BUCKET_NAME
import com.example.data.models.Publication
import com.google.gson.Gson
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.Date

class YcPublicationService(private val s3: AmazonS3): PublicationService {
    override suspend fun getAllPublications(): ArrayList<Publication> {
        val listObjRequest = ListObjectsV2Request().withBucketName(BUCKET_NAME).withPrefix("publications/")
        val objSummaries = s3.listObjectsV2(listObjRequest).objectSummaries.filter { it.key.endsWith(".json") }
        val publications = objSummaries.map { summary ->
            val obj = s3.getObject(BUCKET_NAME, summary.key)
            val objContent = obj.objectContent.bufferedReader().use { it.readText() }
            val publication = Gson().fromJson(objContent, Publication::class.java)
            publication
        }
        return ArrayList(publications)
        /*for (summary in objSummaries) {
            val obj = s3.getObject(BUCKET_NAME, summary.key)
            val objContent = obj.objectContent.bufferedReader().use { it.readText() }
            val publication = Gson().fromJson(objContent, Publication::class.java)
        }*/
    }

    override suspend fun getPublicationByCategory(category: String): List<Publication> {
        return getAllPublications().filter { it.category == category }
    }

    override suspend fun getPublicationById(publicationId: String): Publication? {
        val pubObj = s3.getObject(BUCKET_NAME, "publications/$publicationId.json")
        val pubContent = pubObj.objectContent.bufferedReader().use { it.readText() }
        return Gson().fromJson(pubContent, Publication::class.java)
    }

    override suspend fun insertPublication(publication: Publication): Boolean {
        val pubJson = Gson().toJson(publication)
        val insertPub = s3.putObject(BUCKET_NAME, "publications/${publication.category}/${publication.id}/${publication.id}.json", pubJson)
        return insertPub != null
    }

    override suspend fun insertFile(file: File, fileName: String, publication: Publication): Boolean {
        val result = s3.putObject(BUCKET_NAME, "publications/${publication.category}/${publication.id}/images/$fileName", file)
        return result != null
    }

    fun generatePresignedUrl(objectKey: String): String {
        val exception = Date()
        var expTimeMillis: Long = Instant.now().toEpochMilli()
        expTimeMillis += 1000 * 60 * 60 * 24 * 7
        exception.time = expTimeMillis
        val presignedUrl = s3.generatePresignedUrl(BUCKET_NAME, objectKey, exception)

        return presignedUrl.toString()
    }


    /*override suspend fun getCurrentPublication(publicationId: String): Publication {

    }*/



}