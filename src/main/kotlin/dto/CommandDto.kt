package ru.guap.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.guap.config.AnyMapSerializer
import ru.guap.config.LocalDateSerializer
import java.time.LocalDateTime


@Serializable
data class CommandDto(
    @SerialName("n") var N: Int = 0,
    @SerialName("device") var device: String = "",
    @SerialName("params") @Serializable(AnyMapSerializer::class) var params: Map<String, Any?> = emptyMap(),
    @SerialName("status") var status: String = "",
    @SerialName("timestamp") @Serializable(LocalDateSerializer::class) var timestamp: LocalDateTime = LocalDateTime.now()
)