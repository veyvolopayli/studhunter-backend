package com.example.yandexcloud

import com.amazonaws.services.s3.AmazonS3
import com.example.data.models.User
import com.example.data.userservice.UserDataSource
import com.google.gson.Gson
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.litote.kmongo.eq
import org.litote.kmongo.json
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class YcUserDataSource(private val s3Client: AmazonS3) {

    private val bucketName = "stud-hunter-bucket"

    suspend fun getUserByUsername(username: String): User = suspendCoroutine { cont ->
        val userObject = s3Client.getObject(bucketName, "$username.json")
        val jsonContent = userObject.objectContent.bufferedReader().use { it.readText() }
        cont.resume(Json.decodeFromString<User>(jsonContent))
    }

    fun insertUser(user: User): Boolean {
        val uid = UUID.randomUUID()
        val userJson = Gson().toJson(user)
        val insertUser = s3Client.putObject(bucketName, "${ user.username }.json", userJson)
        return insertUser != null
    }
}