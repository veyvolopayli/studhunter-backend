package com.studhunter.api.application.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateApplicationRequest(
    val vacancyId: String,
    val message: String? = null
)