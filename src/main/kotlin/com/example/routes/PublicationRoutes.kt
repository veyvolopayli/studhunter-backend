package com.example.routes

import ch.qos.logback.classic.Logger
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.example.data.models.Publication
import com.example.data.publicationservice.PublicationService
import com.example.data.publicationservice.YcPublicationService
import com.example.data.requests.PublicationByIdRequest
import com.example.data.requests.PublicationRequest
import com.example.data.responses.PublicationResponse
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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

            val multipart = call.receiveMultipart()
            val parts = mutableListOf<PartData.FileItem>()
            var publicationRequest: PublicationRequest? = null
            var imageUrl = ""

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> parts.add(part)
                    is PartData.FormItem -> {
                        if (part.name == "publicationData") {
                            val jsonString = part.value
                            publicationRequest = Json.decodeFromString<PublicationRequest>(jsonString)
                        }
                    }

                    else -> Unit
                }
                part.dispose
            }

            val publication = Publication(
                imageUrl = "example image url",
                title = publicationRequest?.title ?: "",
                description = publicationRequest?.description ?: "",
                price = publicationRequest?.price ?: "",
                priceType = publicationRequest?.priceType ?: "",
                district = publicationRequest?.district ?: "",
                timeStamp = publicationRequest?.timeStamp ?: "",
                category = publicationRequest?.category ?: "",
                userId = publicationRequest?.userId ?: "",
                socials = publicationRequest?.socials ?: ""
            )

            val publicationUploaded = publicationService.insertPublication(publication)

            if (!publicationUploaded) {
                call.respond(status = HttpStatusCode.BadRequest, message = PublicationResponse(success = false))
                return@post
            }

            val files: List<File> = parts.mapNotNull { part ->
                val random = Random.nextInt(1000, 10000)
                val fileName = "image_$random"
                if (part.name == "images") part.toFile(fileName, ".jpeg")
                else null
            }
            val results = mutableListOf<Boolean>()

            files.forEach { file ->
                file.save("build/resources/main/static/images/", file.name)
                results.add(publicationService.insertFile(file, file.name, publication))
            }

            if (results.contains(false)) {
                call.respond(status = HttpStatusCode.BadRequest, message = PublicationResponse(success = false))
                return@post
            }

            call.respond(status = HttpStatusCode.OK, message = PublicationResponse(success = true))

            /*val request = call.receiveNullable<PublicationRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            publication = Publication(
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

            call.respond(HttpStatusCode.OK)*/
        }
    }

    /*val multipart = call.receiveMultipart()
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
                    publicationService.insertFile(file, fileName, publication)
                }
            }
            else -> Unit
        }
    }*/

    /*post("new-publication-image") {
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
                        publicationService.insertFile(file, fileName, publication)
                    }
                }
                else -> Unit
            }
        }
    }*/
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

fun Route.getPublicationById() {
    authenticate {
        post("publication-by-id") {
            val request = call.receiveNullable<PublicationByIdRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val publication = ycPublicationService.getPublicationById(request.id)

            if (publication == null) {
                call.respond(HttpStatusCode.BadRequest, "Publication doesn't exist!")
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = publication.toString()
            )

        }
    }
}

