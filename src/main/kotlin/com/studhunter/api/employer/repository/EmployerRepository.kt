package com.studhunter.api.employer.repository

import com.studhunter.api.employer.dto.UpdateEmployerProfileRequest
import com.studhunter.api.employer.model.EmployerProfile
import java.util.*
import com.studhunter.api.employer.dto.CreateEmployerProfileRequest as CreateEmployerProfileRequest1

interface EmployerRepository {
    fun insertProfile(userId: UUID, req: CreateEmployerProfileRequest1): UUID?
    fun getProfile(userId: String): EmployerProfile?
    fun updateProfile(userId: String, req: UpdateEmployerProfileRequest): Boolean?
    fun deleteProfile(userId: String): Boolean?
}
