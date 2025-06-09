package ru.guap.controller

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import ru.guap.service.RemoteTerminalService

fun Application.statusesController() {
    val remoteTerminalService by inject<RemoteTerminalService>()
    routing {
        route("/api/statuses") {
            webSocket("/ws/status") {
                remoteTerminalService.getStatusFlow().collect { status ->
                    send(Frame.Text(Json.encodeToString(status)))
                }
            }
            get {
                val status = remoteTerminalService.getStatus()
                call.respond(status)
            }
        }
    }
}