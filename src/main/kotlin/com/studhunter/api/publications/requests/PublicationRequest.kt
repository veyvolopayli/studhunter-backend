package com.studhunter.api.publications.requests

import kotlinx.serialization.Serializable

@Serializable
data class PublicationRequest(
    val title: String,
    val description: String,
    val price: Int,
    val priceType: String,
    val district: String?,
    val category: String,
    val userId: String,
    val socials: String
)