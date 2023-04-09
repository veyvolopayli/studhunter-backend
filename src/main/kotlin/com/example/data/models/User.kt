package com.example.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.UUID

data class User(
    val username: String,
    val password: String,
    val salt: String,
    val id: String = UUID.randomUUID().toString(),
    val rating: Float = 5.0F,
    val fullName: String = ""
    /*@BsonId val id: ObjectId = ObjectId()*/
)