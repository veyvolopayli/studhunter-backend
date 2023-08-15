package com.studhunter.plugins

import com.amazonaws.services.s3.AmazonS3
import com.studhunter.api.auth.routes.*
import com.studhunter.api.chat.routing.chatRoutes
import com.studhunter.api.email.routes.emailRouting
import com.studhunter.api.email.service.EmailService
import com.studhunter.api.publications.repository.PublicationsRepository
import com.studhunter.api.publications.repository.YCloudPublicationsRepository
import com.studhunter.api.publications.routing.*
import com.studhunter.api.reviews.routing.fetchReviews
import com.studhunter.api.reviews.routing.insertReviews
import com.studhunter.api.updates.repository.UpdateRepository
import com.studhunter.api.updates.routes.updateRoutes
import com.studhunter.api.users.repository.UsersRepository
import com.studhunter.api.users.routing.userRouting
import com.studhunter.security.hashing.HashingService
import com.studhunter.security.token.TokenConfig
import com.studhunter.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    yCloudPublicationsRepository: YCloudPublicationsRepository,
    publicationRepository: PublicationsRepository,
    userRepository: UsersRepository,
    s3: AmazonS3,
    emailService: EmailService,
    ycUpdateRepository: UpdateRepository
) {
    routing {
        signIn(hashingService, tokenService, tokenConfig)
        signUp(hashingService, emailService, tokenService, tokenConfig)
        authenticate()
        getSecretInfo()
        getPublicationRoutes()
        postPublicationRoutes(yCloudPublicationsRepository)
        imageRoutes(s3)
        getUserId()
        getUserPubs(publicationRepository)
        userRouting(userRepository)
        fetchReviews()
        insertReviews()
        publicationOperationRoutes()
        emailRouting()
        updateRoutes(ycUpdateRepository)
        chatRoutes()

        get("/") {
            call.respond(
                message = "посоветуйте ониме аниме новичку\nтвое имя\nантон\nа?\nче звал сларк",
                status = HttpStatusCode.OK
            )
        }
    }
}