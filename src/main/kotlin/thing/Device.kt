package ru.guap.thing

interface Device : Connectable {
    var id: Int
    fun deviceName(): String
}