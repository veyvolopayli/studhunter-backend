package com.example.data.models

import com.example.features.getDate
import kotlinx.serialization.Serializable

@Serializable
data class Publication(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val price: Int,
    val priceType: Int,
    val district: String,
    val timestamp: String = getDate(),
    val category: String,
    val userId: String,
    val socials: String,
    val approved: Boolean? = null
)