package ru.guap.controller

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import ru.guap.service.RemoteTerminalService

fun Application.deviceController() {
    val remoteTerminalService by inject<RemoteTerminalService>()
    routing {
        route("api/devices") {
            get {
                val devices = remoteTerminalService.getDevices()
                call.respond(devices)
            }
        }
    }
}