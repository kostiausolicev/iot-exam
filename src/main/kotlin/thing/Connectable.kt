package ru.guap.thing

interface Connectable {
    fun connect()
    fun isConnected(): Boolean
}