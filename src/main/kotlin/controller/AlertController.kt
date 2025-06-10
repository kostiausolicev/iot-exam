package ru.guap.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import ru.guap.service.MonitorService
import kotlin.getValue

fun Application.alertController() {
    val monitorService by inject<MonitorService>()
    routing {
        route("/api/alerts") {
            webSocket("/ws/alerts") {
                monitorService.getLogsFlow().collect { logs ->
                    send(Frame.Text(Json.encodeToString(logs)))
                }
            }
            post("/clear") {
                monitorService.clearLogs()
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}