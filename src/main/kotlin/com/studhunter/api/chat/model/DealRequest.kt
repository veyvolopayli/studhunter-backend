package com.studhunter.api.chat.model

import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@SerialName("DealRequest")
data class DealRequest(
    val jobTime: Long
) : DataTransfer