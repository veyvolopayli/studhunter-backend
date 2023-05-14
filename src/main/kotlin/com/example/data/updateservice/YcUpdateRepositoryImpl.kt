package com.example.data.updateservice

import com.amazonaws.services.s3.AmazonS3
import com.example.BUCKET_NAME

class YcUpdateRepositoryImpl(private val s3: AmazonS3): UpdateRepository {
    override fun getUpdateFile(version: String): ByteArray? {
        val filePrefix = "stud-hunter-$version"
        val fileSuffix = ".apk"
        return try {
            val fileObj = s3.getObject(BUCKET_NAME, "applications/$filePrefix$fileSuffix")
            val inputStream = fileObj.objectContent
            inputStream.readBytes()
        } catch (e: Exception) {
            null
        }
    }

    override fun isUpdateFileExists(version: String): Boolean? {
        val filePrefix = "stud-hunter-$version"
        val fileSuffix = ".apk"

        return try {
            s3.doesObjectExist(BUCKET_NAME, "applications/$filePrefix$fileSuffix")
        } catch (e: Exception) {
            null
        }
    }

    override fun getLastVersionFile(): Pair<ByteArray, String>? {
        return try {

            val updateObjects = s3.listObjectsV2(BUCKET_NAME, "applications/").objectSummaries
            val names = updateObjects.map { it.key }
            val lastVersionFileName = names.max().substringAfter("applications/")

            println(lastVersionFileName)

            val fileObj = s3.getObject(BUCKET_NAME, "applications/$lastVersionFileName")
            val inputStream = fileObj.objectContent

            Pair(inputStream.readBytes(), lastVersionFileName)

        } catch (e: Exception) {
            null
        }


    }

}