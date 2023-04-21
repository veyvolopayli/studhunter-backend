package com.example.data.models

import com.example.features.getDate
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Publication(
    val id: String = UUID.randomUUID().toString(),
    val imageUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val price: String? = null,
    val priceType: String? = null,
    val district: String? = null,
    val timeStamp: String = getDate(),
    val category: String? = null,
    val userId: String? = null,
    val socials: String? = null
)