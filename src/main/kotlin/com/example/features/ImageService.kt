package com.example.features

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

fun toCompressedImage(inputFile: File, outputFile: File, quality: Float) {
    try {
        val inputStream = ImageIO.createImageInputStream(inputFile)
        val reader = ImageIO.getImageReaders(inputStream).next()
        reader.input = inputStream
        val param = reader.defaultReadParam
        val image = reader.read(0, param)
        val output = FileOutputStream(outputFile)
        val writer = ImageIO.getImageWriter(reader)
        val param2 = writer.defaultWriteParam
        param2.compressionMode = ImageWriteParam.MODE_EXPLICIT
        param2.compressionQuality = quality
        writer.output = output
        writer.write(null, IIOImage(image, null, null), param2)
        inputStream.close()
        output.close()
        writer.dispose()
        reader.dispose()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
