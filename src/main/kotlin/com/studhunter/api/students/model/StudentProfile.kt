package com.studhunter.api.students.model

import java.util.*

data class StudentProfile(
    val userId: UUID,
    val major: String? = null,
    val course: Int? = null,
    val skills: String? = null,
    val resumeUrl: String? = null,
    val portfolioUrl: String? = null
)
