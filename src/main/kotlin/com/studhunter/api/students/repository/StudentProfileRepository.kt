package com.studhunter.api.students.repository

import com.studhunter.api.students.model.StudentProfile

interface StudentProfileRepository {

    fun insertProfile(profile: StudentProfile): Boolean?

    fun getProfile(userId: String): StudentProfile?

    fun updateProfile(userId: String, profile: StudentProfile): Boolean?

    fun deleteProfile(userId: String): Boolean?
}
