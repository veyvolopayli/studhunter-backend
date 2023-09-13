package com.studhunter.api.publications_filter.model

import kotlinx.serialization.Serializable

@Serializable
data class FilterRequest(
    val minPrice: Int?,
    val maxPrice: Int?,
    val priceTypes: List<String>?,
    val districts: List<String>?,
    val categories: List<String>?,
    val minUserRating: Int?,
)