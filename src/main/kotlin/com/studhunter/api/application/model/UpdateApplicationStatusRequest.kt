package com.studhunter.api.application.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateApplicationStatusRequest(
    val status: ApplicationStatus
)