package com.studhunter.api.vacancy.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateVacancyRequest(
    val title: String,
    val description: String,
    val category: String,
    val format: String? = null,
    val region: String? = null,
    val schedule: String? = null,
    val payment: String? = null,
    val closesAt: Long? = null
)