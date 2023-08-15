package com.studhunter.api.reviews.model

import kotlinx.serialization.Serializable

@Serializable
data class UserReviews(val reviews: MutableList<Double>)
