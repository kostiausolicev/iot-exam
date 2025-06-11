package ru.guap.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThresholdsDto(
    val m1: Threshold,
    val m2: Threshold,
    val m3: Threshold,
    val m4: Threshold,
    val m5: Threshold,
    val m6: Threshold,

    val t1: Threshold,
    val t2: Threshold,
    val t3: Threshold,
    val t4: Threshold,
    val t5: Threshold,
    val t6: Threshold,

    val l1: Threshold,
    val l2: Threshold,
    val l3: Threshold,
    val l4: Threshold,
    val l5: Threshold,
    val l6: Threshold
)

@Serializable
data class Threshold(
    @SerialName("warn_min") val warnMin: Int,
    @SerialName("warn_max") val warnMax: Int,
    @SerialName("crit_min") val critMin: Int,
    @SerialName("crit_max") val critMax: Int,
)
