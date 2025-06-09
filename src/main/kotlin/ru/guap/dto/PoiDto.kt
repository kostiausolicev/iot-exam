package ru.guap.ru.guap.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PoiDto(
    @SerialName("name") val name: String = "",
    @SerialName("X") val x: Int? = null,
    @SerialName("Y") val y: Int? = null,
    @SerialName("T") val t: Int? = null,
)
