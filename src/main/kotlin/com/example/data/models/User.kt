package com.example.data.models

import java.util.*

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val password: String,
    val salt: String,
    var rating: Double = 5.0,
    val name: String,
    val surname: String?,
    val email: String,
    val university: String?
)