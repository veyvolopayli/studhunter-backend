package com.studhunter.api.publications_filter.model

import kotlinx.serialization.Serializable

@Serializable
data class FilterRequest(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val priceTypes: List<String>? = null,
    val districts: List<String>? = null,
    val categories: List<String>? = null,
    val minUserRating: Int? = null,
)