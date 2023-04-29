package com.example

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.example.data.publicationservice.YcPublicationService
import com.example.data.usersservice.YcUsersService
import com.example.email.EmailService
import io.ktor.server.application.*
import com.example.plugins.*
import com.example.security.hashing.SHA256HashingService
import com.example.security.token.JwtTokenService
import com.example.security.token.TokenConfig
import com.example.yandexcloud.YcUserDataSource
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database


const val BUCKET_NAME = "stud-hunter-bucket"

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        module = Application::module
    ).start(wait = true)
}


fun Application.module() {

    Database.connect(url = "jdbc:postgresql://5.181.255.253:5432/studhunter", driver = "org.postgresql.Driver",
        user = System.getenv("POSTGRES_USERNAME"), password = System.getenv("POSTGRES_PASSWORD"))

    val awsAccessKey = System.getenv("AWS_ACCESS")
    val awsSecretKey = System.getenv("AWS_SECRET")
    val awsCreds = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3 = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(awsCreds))
        .withEndpointConfiguration(
            AwsClientBuilder.EndpointConfiguration(
                "storage.yandexcloud.net", "ru-central1"
            )
        ).build()

    val userDataSource = YcUserDataSource(s3)

    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = "http://0.0.0.0:8080",
        audience = "users",
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )

    val hashingService = SHA256HashingService()

    val publicationService = YcPublicationService(s3)

    val usersService = YcUsersService(s3)

    val emailService = EmailService(
        host = "smtp.yandex.ru",
        port = 465,
        username = "studhunterapp@yandex.ru",
        password = System.getenv("MAIL_YANDEX_PASSWORD"),
        ssl = true,
        senderEmail = "studhunterapp@yandex.ru"
    )

    /*runBlocking {
        withContext(Dispatchers.IO) {
            startServices(usersService, publicationService)
        }
    }*/


    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource, hashingService, tokenService, tokenConfig, publicationService, usersService, s3, emailService)

}