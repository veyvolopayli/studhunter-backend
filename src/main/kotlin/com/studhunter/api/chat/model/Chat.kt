package com.studhunter.api.chat.model

import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import java.util.*
@Serializable
data class Chat(
    val id: String = UUID.randomUUID().toString(),
    val vacancyId: String,        // вакансия-источник чата
    val employerId: String,       // владелец вакансии
    val studentId: String,        // откликнувшийся студент
    var lastMessage: String = "", // текст последнего сообщения
    val createdAt: Long = getTimeMillis()
)
