package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class PublicationByIdRequest(
    val id: String
)