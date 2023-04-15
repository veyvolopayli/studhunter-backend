package com.example

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.example.data.models.User
import com.example.data.publicationservice.PublicationService
import com.example.data.publicationservice.YcPublicationService
import com.example.data.userservice.MongoUserDataSource
import io.ktor.server.application.*
import com.example.plugins.*
import com.example.security.hashing.SHA256HashingService
import com.example.security.token.JwtTokenService
import com.example.security.token.TokenConfig
import com.example.yandexcloud.YcUserDataSource
import io.github.cdimascio.dotenv.Dotenv
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


const val BUCKET_NAME = "stud-hunter-bucket"

fun main(args: Array<String>): Unit =
    io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    /*val mongoPassword = System.getenv("MONGO_PW")
    val dbName = "seefood"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://veyvolopayli:$mongoPassword@cluster0.d8tkum5.mongodb.net/$dbName?retryWrites=true&w=majority"
    ).coroutine.getDatabase(dbName)*/

    val dotEnv: Dotenv = Dotenv.load()

    val awsAccessKey = dotEnv.get("AWS_ACCESS")
    val awsSecretKey = dotEnv.get("AWS_SECRET")
    val awsCreds = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3 = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(awsCreds))
        .withEndpointConfiguration(
            AwsClientBuilder.EndpointConfiguration(
                "storage.yandexcloud.net", "ru-central1"
            )
        ).build()

//    val userDataSource = MongoUserDataSource(db)
    val userDataSource = YcUserDataSource(s3)

    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = dotEnv.get("JWT_SECRET")
    )

    val hashingService = SHA256HashingService()

    val publicationService = YcPublicationService(s3)

    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource, hashingService, tokenService, tokenConfig, publicationService)

}