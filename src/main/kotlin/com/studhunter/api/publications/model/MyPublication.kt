package com.studhunter.api.publications.model

import kotlinx.serialization.Serializable

@Serializable
data class MyPublication(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val price: Int?,
    val priceType: String,
    val timestamp: Long,
    val userId: String,
    val approved: Boolean?,
    val views: Long = 0,
    val favorites: Long = 0
)