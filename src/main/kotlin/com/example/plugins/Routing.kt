package com.example.plugins

import com.amazonaws.services.s3.AmazonS3
import com.example.data.publicationservice.PublicationService
import com.example.data.updateservice.UpdateRepository
import com.example.data.userservice.UserDataSource
import com.example.data.usersservice.UsersService
import com.example.email.EmailService
import com.example.repositories.PublicationRepository
import com.example.repositories.UserRepository
import com.example.routes.*
import com.example.security.hashing.HashingService
import com.example.security.token.TokenConfig
import com.example.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    publicationService: PublicationService,
    publicationRepository: PublicationRepository,
    userRepository: UserRepository,
    usersService: UsersService,
    s3: AmazonS3,
    emailService: EmailService,
    ycUpdateRepository: UpdateRepository
) {
    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService, userDataSource, emailService, tokenService, tokenConfig)
        authenticate()
        getSecretInfo()
        getPublicationRoutes()
        postPublicationRoutes(publicationService)
        imageRoutes(s3)
        getUserId()
        getUserPubs(publicationRepository)
        userRouting(userRepository)
        fetchReviews()
        insertReviews()
        publicationOperationRoutes()
        emailRouting()
        updateRoutes(ycUpdateRepository)

        get("/") {
            call.respond(
                message = "посоветуйте ониме аниме новичку\nтвое имя\nантон\nа?\nче звал сларк",
                status = HttpStatusCode.OK
            )
        }
    }
}