package ru.guap.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import ru.guap.controller.alertController
import ru.guap.controller.commandController
import ru.guap.controller.dataController
import ru.guap.controller.deviceController
import ru.guap.controller.poiController
import ru.guap.controller.statusesController
import kotlin.time.Duration.Companion.seconds

fun Application.configureRouting() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 300.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    commandController()
    statusesController()
    poiController()
    deviceController()
    dataController()
    alertController()
}
