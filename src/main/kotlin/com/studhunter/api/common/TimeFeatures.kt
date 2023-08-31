package com.studhunter.api.common

fun convertHoursToMillis(hours: Long): Long = 1000 * 60 * 60 * hours
fun convertDaysToMillis(days: Long): Long = convertHoursToMillis(24) * days
fun convertWeeksToMillis(weeks: Long): Long = convertDaysToMillis(7) * weeks