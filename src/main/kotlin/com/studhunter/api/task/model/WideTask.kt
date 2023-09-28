package com.studhunter.api.task.model

import com.studhunter.api.chat.model.Task
import com.studhunter.api.users.responses.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class WideTask(
    val task: Task,
    val executor: UserResponse
)