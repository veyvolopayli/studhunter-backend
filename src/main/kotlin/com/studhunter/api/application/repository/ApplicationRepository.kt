package com.studhunter.api.application.repository

import com.studhunter.api.application.model.Application
import com.studhunter.api.application.model.ApplicationStatus
import com.studhunter.api.application.model.CreateApplicationRequest
import java.util.*

interface ApplicationRepository {
    fun insert(req: CreateApplicationRequest, studentId: UUID): UUID?
    fun get(id: String): Application?
    fun getByVacancy(vacancyId: String): List<Application>
    fun getByStudent(studentId: String): List<Application>
    fun updateStatus(applicationId: String, status: ApplicationStatus): Boolean
}