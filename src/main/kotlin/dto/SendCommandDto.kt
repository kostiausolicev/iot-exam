package ru.guap.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendCommandDto(
    val deviceId: Int,
    val X: Int, // Координата X (мм)
    val Y: Int, // Координата Y (мм)
    val T: Int, // Угол поворота -90..90 градусов
    val G: Int = 0, // Механический
    val V: Int = 0, // Вакумный хват
    val lights: List<Boolean>
)
