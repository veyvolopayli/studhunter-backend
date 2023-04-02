package com.example.data.requests

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PublicationRequest(
    val title: String,
    val description: String,
    val price: String,
    val priceType: String,
    val district: String,
    val timeStamp: String,
    val category: String,
    val userId: String,
    val socials: String
)