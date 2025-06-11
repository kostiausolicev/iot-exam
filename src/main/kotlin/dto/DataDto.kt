package ru.guap.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataDto(
    @SerialName("device") val deviceName: String,
    var n: Int,
    val m1: Int? = null,
    val m2: Int? = null,
    val m3: Int? = null,
    val m4: Int? = null,
    val m5: Int? = null,
    val m6: Int? = null,

    val t1: Int? = null,
    val t2: Int? = null,
    val t3: Int? = null,
    val t4: Int? = null,
    val t5: Int? = null,
    val t6: Int? = null,

    val l1: Int? = null,
    val l2: Int? = null,
    val l3: Int? = null,
    val l4: Int? = null,
    val l5: Int? = null,
    val l6: Int? = null,

    @SerialName("X") val x: Int? = null,
    @SerialName("Y") val y: Int? = null,
    @SerialName("T") val t: Int? = null,

    val code: String? = null,
    val p: Int? = null,
    val b1: Int? = null,
    val b2: Int? = null,
    val b3: Int? = null,
)
