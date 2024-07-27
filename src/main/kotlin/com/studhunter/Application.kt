package com.studhunter

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.studhunter.api.email.service.EmailService
import com.studhunter.api.publications.tables.Publications
import com.studhunter.api.publications.yandex_cloud.YCloudPublicationRepositoryImpl
import com.studhunter.api.updates.yandex_cloud.YcUpdateRepositoryImpl
import com.studhunter.api.users.tables.Users
import com.studhunter.plugins.*
import com.studhunter.security.hashing.SHA256HashingService
import com.studhunter.security.token.JwtTokenService
import com.studhunter.security.token.TokenConfig
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database


const val BUCKET_NAME = "stud-hunter-bucket"

fun main() {
    embeddedServer(
        Netty,
        port = 8081,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {

    val dotEnv = dotenv { filename = "config.env" }

    Database.connect(
        url = "jdbc:postgresql://5.181.255.253:5432/studhunter", driver = "org.postgresql.Driver",
        user = dotEnv["POSTGRES_USERNAME"], password = dotEnv["POSTGRES_PASSWORD"]
    )

    val awsAccessKey = dotEnv["AWS_ACCESS"]
    val awsSecretKey = dotEnv["AWS_SECRET"]
    val awsCreds = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3 = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(awsCreds))
        .withEndpointConfiguration(
            AwsClientBuilder.EndpointConfiguration(
                "storage.yandexcloud.net", "ru-central1"
            )
        ).build()

    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = "http://0.0.0.0:8080",
        audience = "users",
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = dotEnv["JWT_SECRET"]
    )

    val hashingService = SHA256HashingService()

    val yCloudPublicationsRepository = YCloudPublicationRepositoryImpl(s3)

    val emailService = EmailService(
        host = "smtp.yandex.ru",
        port = 465,
        username = "studhunterapp@yandex.ru",
        password = dotEnv["MAIL_YANDEX_PASSWORD"],
        ssl = true,
        senderEmail = "studhunterapp@yandex.ru"
    )

    val ycUpdateRepository = YcUpdateRepositoryImpl(s3)

    val publicationRepository = Publications
    val userRepository = Users

    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = tokenConfig,
        yCloudPublicationsRepository = yCloudPublicationsRepository,
        publicationRepository = publicationRepository,
        userRepository = userRepository,
        s3 = s3,
        emailService = emailService,
        ycUpdateRepository = ycUpdateRepository
    )

}