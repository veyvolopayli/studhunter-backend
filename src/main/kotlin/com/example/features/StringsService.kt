package com.example.features

import java.security.MessageDigest

fun generateFixedLengthString(str1: String, str2: String, length: Int): String {
    val input = "$str1$str2"
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.toByteArray())
    val hashString = hashBytes.joinToString("") { "%02x".format(it) }
    return hashString.substring(0, length)
}


fun String.hashed(length: Int): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(this.toByteArray())
    val hashString = hashBytes.joinToString("") { "%02x".format(it) }
    return hashString.substring(0, length)
}