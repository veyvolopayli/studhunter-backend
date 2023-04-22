package com.example.data.publicationservice

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.*
import com.example.BUCKET_NAME
import com.example.data.constants.ASSEMBLED_P_NAME
import com.example.data.constants.NEW_PUBS_PATH
import com.example.data.constants.PUBS_PATH
import com.example.data.constants.USERS_DATA_PATH
import com.example.data.models.PubIds
import com.example.data.models.Publication
import com.example.yandexcloud.YcPaths
import com.google.gson.Gson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.util.Date

class YcPublicationService(private val s3: AmazonS3): PublicationService {

    private val ycPaths = YcPaths()

    override suspend fun getAllPublications(): List<Publication> {
        val listObjRequest = ListObjectsV2Request().withBucketName(BUCKET_NAME).withPrefix(NEW_PUBS_PATH)
//        val objSummaries = s3.listObjectsV2(listObjRequest).objectSummaries.filter { it.key.endsWith(".json") }
        val objSummaries = s3.listObjectsV2(listObjRequest).objectSummaries
        val publications = objSummaries.mapNotNull { summary ->
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

    override suspend fun getPublicationsByCategory(category: String): List<Publication> {
        val listObjRequest = ListObjectsV2Request().withBucketName(BUCKET_NAME).withPrefix("$NEW_PUBS_PATH/$category")
        val objSummaries = s3.listObjectsV2(listObjRequest).objectSummaries
        val publications = objSummaries.map { summary ->
            val obj = s3.getObject(BUCKET_NAME, summary.key)
            val objContent = obj.objectContent.bufferedReader().use { it.readText() }
            val publication = Gson().fromJson(objContent, Publication::class.java)
            publication
        }
        return publications
    }

    override suspend fun getPublicationById(category: String, publicationId: String): Publication? {
        return try {
            val pubObj = s3.getObject(BUCKET_NAME, ycPaths.newPubPath(category, publicationId))
            val pubContent = pubObj.objectContent.bufferedReader().use { it.readText() }
            Gson().fromJson(pubContent, Publication::class.java)
        } catch (e: AmazonS3Exception) {
            if (e.statusCode == 404) null
            else throw e
        }
    }

    override suspend fun insertPublication(publication: Publication): Boolean {
        val pubJson = Gson().toJson(publication)
        val insertPub = s3.putObject(BUCKET_NAME, ycPaths.newPubPath(publication.category ?: "Другое", publication.id), pubJson)
        addPublicationToUser(userId = publication.userId ?: "0", pubId = publication.id, category = publication.category ?: "Другое")
        return insertPub != null
    }

    override suspend fun insertPublicationImage(file: File, fileName: String, pubId: String): Boolean {
        val result = s3.putObject(BUCKET_NAME, ycPaths.newPubImagePath(pubId, fileName), file)
        return result != null
    }

    data class TemporaryImage(
        val url: String? = null,
        val expiresIn: Date? = null
    )

    override suspend fun generateTemporaryImageUrl(category: String, pubId: String, fileName: String): TemporaryImage {
        val date = Date()
        var expTimeMillis: Long = Instant.now().toEpochMilli()
        expTimeMillis += 1000 * 60 * 60 * 24 * 7
        date.time = expTimeMillis
        val temporaryUrl =
            s3.generatePresignedUrl(BUCKET_NAME, "publications/images/$category/$pubId/$fileName.jpeg", date).toString()

        return TemporaryImage(url = temporaryUrl, expiresIn = date)
    }

    override suspend fun addPublicationToUser(userId: String, pubId: String, category: String): Boolean {
        var result: PutObjectResult?
        val path = "$USERS_DATA_PATH/$userId/user_pubs.json"
        val userPubIds = getUserPubIds(userId) ?: kotlin.run {
            val newListOfIds = mutableMapOf(pubId to category)
            val newJson = Gson().toJson(PubIds(newListOfIds))
            result = s3.putObject(BUCKET_NAME, path, newJson)
            return result != null
        }

        userPubIds.ids[pubId] = category

        val updatedJson = Gson().toJson(userPubIds)

        result = s3.putObject(BUCKET_NAME, path, updatedJson)

        return result != null
    }

    override suspend fun getUserPubIds(userId: String): PubIds? {
        val obj = try {
            s3.getObject(BUCKET_NAME, "$USERS_DATA_PATH/$userId/user_pubs.json")
        } catch (e: Exception) {
            return null
        }
        val jsonString = obj.objectContent.bufferedReader().use { it.readText() }

        return Json.decodeFromString<PubIds>(jsonString)
    }

    override suspend fun startPublicationsTask() {
        val success = updatePublications()

        if (success) println("Publications successfully updated")
    }

    override suspend fun insertAssembledPublications(publications: List<Publication>): Boolean {
        val publicationsJson = Gson().toJson(publications)
        val result = s3.putObject(BUCKET_NAME, "$PUBS_PATH/$ASSEMBLED_P_NAME", publicationsJson)

        return result != null
    }

    override suspend fun updatePublications(): Boolean {
        val publications = getAllPublications()

        return insertAssembledPublications(publications)
    }

    override suspend fun getAssembledPublications(): String? {
        val publicationsObj = try {
            s3.getObject(BUCKET_NAME, "$PUBS_PATH/$ASSEMBLED_P_NAME")
        } catch (e: Exception) { null }

        return publicationsObj?.objectContent?.bufferedReader().use { it?.readText() }
    }


    /*override suspend fun getCurrentPublication(publicationId: String): Publication {

    }*/



}