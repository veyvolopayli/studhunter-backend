package com.studhunter.api.publications.repository

interface PublicationViewsRepository {
    fun insertView(publicationId: String, username: String): Boolean?
    fun fetchViewsCount(publicationId: String): Long
}