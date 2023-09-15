package com.studhunter.api.common

fun <T> List<T>?.nullifyEmptyList(): List<T>? = this?.ifEmpty { null }