package ru.guap.dto

import kotlinx.serialization.Serializable

// Главный DTO для статуса, содержащий списки роботов и состояние ламп
@Serializable
data class StatusDTO(
    val robots: List<RobotStatusDTO>,
    val lamps: LampsDTO?
)

// DTO для статуса отдельного робота
@Serializable
data class RobotStatusDTO(
    val name: String,
    val X: Int,
    val Y: Int,
    val T: Int,
    val s: Int
)

// DTO для состояния ламп
@Serializable
data class LampsDTO(
    val L1: Int,
    val L2: Int,
    val L3: Int,
    val L4: Int
)
