package ru.guap.config

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import ru.guap.controller.commandController
import ru.guap.controller.poiController
import ru.guap.controller.statusesController
import kotlin.time.Duration.Companion.seconds

fun Application.configureRouting() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(text = "400: $cause", status = HttpStatusCode.BadRequest)
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 300.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    commandController()
    statusesController()
    poiController()
}
