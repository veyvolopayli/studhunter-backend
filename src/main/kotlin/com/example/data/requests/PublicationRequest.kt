package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class PublicationRequest(
    val title: String,
    val description: String,
    val price: Int,
    val priceType: Int,
    val district: String?,
    val category: String,
    val userId: String,
    val socials: String
)