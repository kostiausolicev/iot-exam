package ru.guap.dto

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonProperty
import ru.guap.config.AnyMapSerializer
import ru.guap.config.LocalDateSerializer
import java.time.LocalDateTime

@Serializable
data class CommandDto(
    @BsonProperty("N") var N: Int = 0,
    @BsonProperty("device") var device: String = "",
    @BsonProperty("params") @Serializable(AnyMapSerializer::class) var params: Map<String, Any?> = emptyMap(),
    @BsonProperty("status") var status: String = "",
    @BsonProperty("timestamp") @Serializable(LocalDateSerializer::class) var timestamp: LocalDateTime = LocalDateTime.now()
)