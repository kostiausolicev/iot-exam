package ru.guap.thing.smart.lamp

import kotlinx.coroutines.delay
import ru.guap.thing.Device

class SmartLamp(override var id: Int) : Device {
    private var status: Boolean = false
    private var l1: Boolean = false // Синяя - выполнение
    private var l2: Boolean = false // Красная - авария
    private var l3: Boolean = false // Желтая - обслуживание
    private var l4: Boolean = false // Зеленая - ожидание

    suspend fun setLight(lights: List<String>, callback: (suspend () -> Unit)? = null) {
        lights.forEach { light ->
            when (light) {
                "L1" -> { l1 = true; l2 = false; l3 = false; l4 = false }
                "L2" -> { l1 = false; l2 = true; l3 = false; l4 = false }
                "L3" -> { l1 = false; l2 = false; l3 = true; l4 = false }
                "L4" -> { l1 = false; l2 = false; l3 = false; l4 = true }
                else -> throw IllegalArgumentException("Unknown light: $light")
            }
        }
        delay(10_000)
        callback?.invoke()
    }

    fun getLights(): List<Boolean> =
        listOf(l1, l2, l3, l4)

    override fun connect() {
        status = true
    }

    override fun isConnected(): Boolean {
        return status
    }

    override fun deviceName(): String = "SmartLamp"
}