package ru.guap.controller

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import ru.guap.service.MonitorService


fun Application.dataController() {
    val monitorService by inject<MonitorService>()
    routing {
        webSocket("/api/monitor/ws/data") {
            monitorService.getDataFlow().collect { data ->
                send(Frame.Text(Json.encodeToString(mapOf("raw" to data))))
            }
        }
    }
}