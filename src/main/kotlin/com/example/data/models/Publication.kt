package com.example.data.models

import com.example.data.constants.HOST
import com.example.features.getCurrentMills
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Publication(
    val id: String = UUID.randomUUID().toString(),
    val imageUrl: String = "$HOST/image/$id/image_0",
    val title: String,
    val description: String,
    val price: Int?,
    val priceType: String,
    val district: String?,
    val timestamp: Long = getCurrentMills(),
    val category: String,
    val userId: String,
    val socials: String,
    val approved: Boolean? = null
)