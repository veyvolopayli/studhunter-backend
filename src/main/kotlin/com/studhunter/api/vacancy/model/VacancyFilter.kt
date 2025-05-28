package com.studhunter.api.vacancy.model

import kotlinx.serialization.Serializable

@Serializable
data class VacancyFilter(
    val category: String? = null,
    val region: String? = null,
    val format: String? = null
)