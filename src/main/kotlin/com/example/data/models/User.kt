package com.example.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.UUID

data class User(
    val username: String,
    val password: String,
    val salt: String,
    val id: String = UUID.randomUUID().toString()
    /*@BsonId val id: ObjectId = ObjectId()*/
)
