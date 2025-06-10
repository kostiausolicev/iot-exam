package ru.guap.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataPhysDto(
    @SerialName("device") val deviceName: String,
    var n: Int,
    @SerialName("theta1") val m1: Int? = null,
    @SerialName("theta2") val m2: Int? = null,
    @SerialName("theta3") val m3: Int? = null,
    @SerialName("theta4") val m4: Int? = null,
    @SerialName("theta5") val m5: Int? = null,
    @SerialName("theta6") val m6: Int? = null,

    @SerialName("T1") val t1: Int? = null,
    @SerialName("T2") val t2: Int? = null,
    @SerialName("T3") val t3: Int? = null,
    @SerialName("T4") val t4: Int? = null,
    @SerialName("T5") val t5: Int? = null,
    @SerialName("T6") val t6: Int? = null,

    @SerialName("L1") val l1: Int? = null,
    @SerialName("L2") val l2: Int? = null,
    @SerialName("L3") val l3: Int? = null,
    @SerialName("L4") val l4: Int? = null,
    @SerialName("L5") val l5: Int? = null,
    @SerialName("L6") val l6: Int? = null,

    @SerialName("X") val x: Int? = null,
    @SerialName("Y") val y: Int? = null,
    @SerialName("Tpos") val t: Int? = null,

    val code: String? = null,
    val p: Int? = null,
    val b1: Int? = null,
    val b2: Int? = null,
    val b3: Int? = null,
)