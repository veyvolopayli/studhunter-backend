package com.studhunter.api.features

import io.ktor.http.content.*
import net.coobird.thumbnailator.Thumbnails
import java.io.File
import java.io.InputStream

fun PartData.FileItem.save(path: String, fileName: String): String {
    val fileBytes = streamProvider().readBytes()
    val folder = File(path)
    folder.mkdirs()
    File("$path$fileName").writeBytes(fileBytes)
    return fileName
}

fun PartData.FileItem.toFile(prefix: String, suffix: String): File {
    val fileBytes = streamProvider().readBytes()
    val file = File.createTempFile(prefix, suffix)
    file.writeBytes(fileBytes)
    return file
}

fun PartData.FileItem.toCompressedImage(quality: Double, prefix: String, suffix: String): File? {
    return streamProvider().toCompressedImage(quality, prefix, suffix)
}

fun File.deleteFile(): Boolean? {
    return try {
        this.delete()
    } catch (e: Exception) {
        null
    }
}

fun InputStream.toCompressedImage(quality: Double, prefix: String, suffix: String): File? {
    return try {
        val outputFile = File.createTempFile(prefix, suffix)
        Thumbnails.of(this)
            .size(1080, 1080)
            .outputFormat("JPEG")
            .outputQuality(quality)
            .toFile(outputFile)
        outputFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun File.save(path: String, fileName: String) {
    val folder = File(path)
    folder.mkdirs()
    val file = File("$path$fileName.jpeg")
    this.copyTo(file)
}