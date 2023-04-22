package com.example.features

import com.example.data.publicationservice.PublicationService
import com.example.data.usersservice.UsersService
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun getCurrentMills(): Long = getTimeMillis()

fun getDate(): String {
    val moscowZoneId = ZoneId.of("Europe/Moscow")
    val moscowDateTime = LocalDateTime.now(moscowZoneId)
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
    return moscowDateTime.format(formatter)
}

fun startServices(usersService: UsersService, publicationService: PublicationService) {
    val timer = Timer()
    val task = object : TimerTask() {
        override fun run() {
            CoroutineScope(Dispatchers.IO).launch {
                usersService.startReviewsTask()
                publicationService.startPublicationsTask()
            }
        }
    }
    val hourInMills = 1000L * 60L * 60L
    val delay = 0L
    timer.scheduleAtFixedRate(task, delay, hourInMills)
}