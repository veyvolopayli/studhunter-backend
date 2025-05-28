package com.studhunter.api.employer.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateEmployerProfileRequest(
    val name: String,
    val description: String? = null,
    val website: String? = null,
    val logoUrl: String? = null
)
