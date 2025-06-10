package ru.guap.dto

import kotlinx.serialization.Serializable
import ru.guap.config.LocalDateSerializer
import java.time.LocalDateTime

@Serializable
data class ChartsDto(
    @Serializable(LocalDateSerializer::class) val timestamps: List<LocalDateTime>,
)
