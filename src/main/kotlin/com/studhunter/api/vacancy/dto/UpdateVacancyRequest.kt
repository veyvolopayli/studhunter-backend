package com.studhunter.api.vacancy.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateVacancyRequest(
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val format: String? = null,
    val region: String? = null,
    val schedule: String? = null,
    val payment: String? = null,
    val closesAt: Long? = null,
    val active: Boolean? = null
)