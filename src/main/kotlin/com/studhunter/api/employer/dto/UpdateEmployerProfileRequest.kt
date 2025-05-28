package com.studhunter.api.employer.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEmployerProfileRequest(
    val name: String? = null,
    val description: String? = null,
    val website: String? = null,
    val logoUrl: String? = null
)
