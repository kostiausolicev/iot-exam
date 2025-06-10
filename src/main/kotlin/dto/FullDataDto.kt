package ru.guap.dto

import kotlinx.serialization.Serializable

@Serializable
data class FullDataDto(
    val raw: List<DataDto>,
    val physical: List<DataPhysDto>
)
