package com.studhunter.api.vacancy.repository

import com.studhunter.api.vacancy.dto.CreateVacancyRequest
import com.studhunter.api.vacancy.dto.UpdateVacancyRequest
import com.studhunter.api.vacancy.model.Vacancy
import com.studhunter.api.vacancy.model.VacancyFilter
import java.util.*

interface VacancyRepository {
    fun insertVacancy(employerId: UUID, req: CreateVacancyRequest): UUID?
    fun getVacancy(id: String): Vacancy?
    fun updateVacancy(id: String, req: UpdateVacancyRequest): Boolean?
    fun deleteVacancy(id: String, employerId: String): Boolean?
    fun search(filter: VacancyFilter): List<Vacancy>?
}