package ru.guap.thing.smart.lamp

import ru.guap.thing.Connectable

class SmartLamp : Connectable {
    private var status: Boolean = false
    private var l1: Boolean = false // Синяя - выполнение
    private var l2: Boolean = false // Красная - авария
    private var l3: Boolean = false // Желтая - обслуживание
    private var l4: Boolean = false // Зеленая - ожидание

    fun setLight(lights: List<Boolean>): List<Boolean> {
        lights.forEachIndexed { i, light ->
            when (i) {
                0 -> l1 = light
                1 -> l2 = light
                2 -> l3 = light
                3 -> l4 = light
            }
        }
        return lights
    }

    fun getLights(): List<Boolean> =
        listOf(l1, l2, l3, l4)

    override fun connect() {
        status = true
    }

    override fun isConnected(): Boolean {
        return status
    }
}