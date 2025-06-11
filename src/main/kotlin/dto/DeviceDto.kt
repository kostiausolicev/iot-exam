package ru.guap.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    var id: Int,
    var name: String,
)
