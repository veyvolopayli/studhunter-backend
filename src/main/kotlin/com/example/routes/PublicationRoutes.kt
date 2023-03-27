package com.example.routes

import ch.qos.logback.classic.Logger
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.example.data.models.Publication
import com.example.data.publicationservice.PublicationService
import com.example.data.publicationservice.YcPublicationService
import com.example.data.requests.PublicationRequest
import com.example.features.save
import com.example.features.toFile
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.logging.Log
import java.io.File
import kotlin.random.Random

private val awsAccessKey = System.getenv("AWS_ACCESS")
private val awsSecretKey = System.getenv("AWS_SECRET")
private val awsCreds = BasicAWSCredentials(awsAccessKey, awsSecretKey)
private val s3Client = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(awsCreds))
    .withEndpointConfiguration(
        AwsClientBuilder.EndpointConfiguration(
            "storage.yandexcloud.net", "ru-central1"
        )
    ).build()

private val ycPublicationService = YcPublicationService(s3Client)
//private val publicationService = PublicationService

/*fun Route.uploadImage(publicationService: PublicationService) {

}*/

fun Route.createPublication(publicationService: PublicationService) {
    authenticate {
        post("new-publication") {
            val request = call.receiveNullable<PublicationRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val publication = Publication(
                imageUrl = request.imageUrl,
                title = request.title,
                description = request.description,
                price = request.price,
                priceType = request.priceType,
                district = request.district,
                timeStamp = request.timeStamp,
                category = request.category,
                userId = request.userId
            )
            val isSuccessful = publicationService.insertPublication(publication)
            if (!isSuccessful) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }
            call.respond(HttpStatusCode.OK)
        }

        post("new-publication/image") {
            val multipart = call.receiveMultipart()
            val files = mutableListOf<File>()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> Unit
                    is PartData.FileItem -> {
                        if (part.name == "images") {
                            val random = Random.nextInt(1000, 10000)
                            val fileName = "image_$random.png"
                            val file = part.toFile(fileName, ".jpg")
                            file.save("build/resources/main/static/images/", fileName)
                            publicationService.insertFile(file, fileName)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}

fun Route.getAllPublications(publicationService: PublicationService) {
    authenticate {
        get("publications") {
            call.respond(
                status = HttpStatusCode.OK,
                message = publicationService.getAllPublications().toString()
            )
        }
    }
}

