package com.studhunter.api.common

import kotlinx.coroutines.*
import java.util.Timer

fun convertHoursToMillis(hours: Long): Long = 1000 * 60 * 60 * hours
fun convertDaysToMillis(days: Long): Long = convertHoursToMillis(24) * days
fun convertWeeksToMillis(weeks: Long): Long = convertDaysToMillis(7) * weeks

suspend fun CoroutineScope.startTask(delay: Long, whatToDo : () -> (Unit)) {
    while (isActive) {
        whatToDo.invoke()
        delay(delay)
    }
}