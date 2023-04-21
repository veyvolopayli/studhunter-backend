package com.example.features

import io.ktor.http.content.*
import java.io.File

fun PartData.FileItem.save(path: String, fileName: String): String {
    val fileBytes = streamProvider().readBytes()
    val folder = File(path)
    folder.mkdirs()
    File("$path$fileName").writeBytes(fileBytes)
    return fileName
}

fun PartData.FileItem.toFile(prefix: String, suffix: String) : File {
    val fileBytes = streamProvider().readBytes()
    val file = File.createTempFile(prefix, suffix)
    file.writeBytes(fileBytes)
    return file
}

fun File.save(path: String, fileName: String) {
    val folder = File(path)
    folder.mkdirs()
    val file = File("$path$fileName.jpeg")
    this.copyTo(file)
}