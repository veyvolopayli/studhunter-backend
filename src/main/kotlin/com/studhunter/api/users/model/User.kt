package com.studhunter.api.users.model

import java.util.*

data class User(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val password: String,
    val salt: String,
    var rating: Double = 5.0,
    val name: String,
    val surname: String?,
    val email: String,
    val university: String?,
    val role: UserRole
)