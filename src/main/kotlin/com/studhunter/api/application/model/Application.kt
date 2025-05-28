package com.studhunter.api.application.model

import java.util.*

data class Application(
    val id: UUID,
    val vacancyId: UUID,
    val studentId: UUID,
    val message: String? = null,
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val createdAt: Long
)