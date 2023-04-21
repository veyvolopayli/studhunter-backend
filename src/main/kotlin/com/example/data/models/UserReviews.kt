package com.example.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserReviews(val reviews: MutableList<Float>)
