package com.studhunter.api.publications.requests

import kotlinx.serialization.Serializable

@Serializable
data class ApprovePublicationRequest(
    val publicationId: String,
    val approved: Boolean
)
