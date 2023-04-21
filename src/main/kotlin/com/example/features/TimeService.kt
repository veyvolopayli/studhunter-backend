package com.example.features

import io.ktor.util.date.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun getCurrentMills(): Long = getTimeMillis()

fun getDate(): String {
    val moscowZoneId = ZoneId.of("Europe/Moscow")
    val moscowDateTime = LocalDateTime.now(moscowZoneId)
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
    return moscowDateTime.format(formatter)
}