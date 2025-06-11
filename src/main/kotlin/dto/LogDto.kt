package ru.guap.dto

import kotlinx.serialization.Serializable
import ru.guap.config.LocalDateSerializer
import java.time.LocalDateTime

@Serializable
data class LogDto(
    val level: String,
    @Serializable(LocalDateSerializer::class) val timestamp: LocalDateTime,
    val message: String?,
)
