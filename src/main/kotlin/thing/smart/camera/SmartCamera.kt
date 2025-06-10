package ru.guap.thing.smart.camera

import ru.guap.dto.DataDto
import ru.guap.dto.DataPhysDto
import ru.guap.thing.Device

class SmartCamera(override var id: Int) : Device() {
    private var status: Boolean = false

    fun read() {

    }

    override fun connect() {
        status = true
    }

    override fun isConnected(): Boolean {
        return status
    }

    override fun deviceName(): String = "SmartCamera"
    override fun toDataDto(n: Int): DataDto {
        TODO("Not yet implemented")
    }

    override fun toDataPsycDto(n: Int): DataPhysDto {
        TODO("Not yet implemented")
    }
}