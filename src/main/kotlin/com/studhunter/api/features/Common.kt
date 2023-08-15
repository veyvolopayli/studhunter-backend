package com.studhunter.api.features

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun ApplicationCall.getAuthenticatedUserID(): String? {
    val principal = principal<JWTPrincipal>()
    return principal?.getClaim("userId", String::class)
}