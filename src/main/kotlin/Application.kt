package ru.guap

import io.ktor.server.application.*
import ru.guap.config.configureFrameworks
import ru.guap.config.configureHTTP
import ru.guap.config.configureRouting
import ru.guap.config.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureFrameworks()
    configureRouting()
}
