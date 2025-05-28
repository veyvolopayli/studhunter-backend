package com.studhunter.api.employer.model

import java.util.*

data class EmployerProfile(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val description: String? = null,
    val website: String? = null,
    val logoUrl: String? = null
)
