package com.example.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val password: String,
    val salt: String,
    var rating: Float = 5.0F,
    val fullName: String,
    val email: String
    /*@BsonId val id: ObjectId = ObjectId()*/
)