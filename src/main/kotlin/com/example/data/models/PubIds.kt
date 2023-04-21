package com.example.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PubIds(val ids: MutableMap<String, String>)