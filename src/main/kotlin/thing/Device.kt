package ru.guap.thing

abstract class Device : Connectable {
    var running: Boolean = false
    abstract var id: Int
    abstract fun deviceName(): String
}