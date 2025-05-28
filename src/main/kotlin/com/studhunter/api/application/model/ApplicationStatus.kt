package com.studhunter.api.application.model

import kotlinx.serialization.Serializable

@Serializable
enum class ApplicationStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}