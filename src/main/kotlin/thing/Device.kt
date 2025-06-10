package ru.guap.thing

import ru.guap.dto.DataDto
import ru.guap.dto.DataPhysDto

abstract class Device : Connectable {
    var running: Boolean = false
    abstract var id: Int
    abstract fun deviceName(): String
    abstract fun toDataDto(n: Int): DataDto
    abstract fun toDataPsycDto(n: Int): DataPhysDto
}