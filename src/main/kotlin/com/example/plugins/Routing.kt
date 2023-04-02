package com.example.plugins

import com.example.data.publicationservice.PublicationService
import com.example.data.userservice.UserDataSource
import com.example.routes.*
import com.example.security.hashing.HashingService
import com.example.security.token.TokenConfig
import com.example.security.token.TokenService
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    publicationService: PublicationService
) {
    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService, userDataSource)
        authenticate()
        getSecretInfo()
        createPublication(publicationService)
        getAllPublications(publicationService)
        getPublicationById()
//        uploadImage(publicationService)
    }
}