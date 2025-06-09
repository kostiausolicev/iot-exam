package ru.guap.thing.smart.camera

import ru.guap.thing.Connectable

class SmartCamera : Connectable {
    private var status: Boolean = false

    fun read() {

    }

    override fun connect() {
        status = true
    }

    override fun isConnected(): Boolean {
        return status
    }
}