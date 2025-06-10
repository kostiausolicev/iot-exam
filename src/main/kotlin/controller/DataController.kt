package ru.guap.controller

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket


fun Application.dataController() {
    routing {
        webSocket("/ws/data") {

        }
    }
}