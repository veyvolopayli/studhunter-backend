package com.studhunter.api.vacancy.model

import java.util.*

data class Vacancy(
    val id: UUID,
    val employerId: UUID,
    val title: String,
    val description: String,
    val category: String,
    val format: String?,        // офлайн | онлайн | гибрид
    val region: String?,        // регион/город
    val schedule: String?,      // полный день, частичный, гибкий …
    val payment: String?,       // “не оплачивается” | “30 000 руб” …
    val createdAt: Long,
    val closesAt: Long? = null,
    val active: Boolean = true
)
