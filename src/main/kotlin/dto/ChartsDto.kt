package ru.guap.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.guap.config.LocalDateTimeListSerializer
import java.time.LocalDateTime

@Serializable
data class ChartsDto(
    @Serializable(LocalDateTimeListSerializer::class) val timestamps: List<LocalDateTime>,
    @SerialName("X") val x: List<Int>,
    @SerialName("Y") val y: List<Int>,
    @SerialName("T") val t: List<Int>,
)
