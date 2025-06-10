package ru.guap.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import ru.guap.service.MonitorService
import java.time.LocalDateTime


fun Application.dataController() {
    val monitorService by inject<MonitorService>()
    routing {
        webSocket("/api/monitor/ws/data") {
            monitorService.getDataFlow().collect { data ->
                send(Frame.Text(Json.encodeToString(data)))
            }
        }
        get("/api/data") {
            val start = call.queryParameters["from"]!!.let { LocalDateTime.parse(it) }
            val end = call.queryParameters["to"]!!.let { LocalDateTime.parse(it) }
            val ms = call.queryParameters["ms"]!!.split(",").map { it.trim() }

//            val data = monitorService.getData(start, end, ms)
//            call.respond(data)
        }

        get("/api/thresholds") {
            val thresholds = monitorService.getThresholds() ?: call.respond(HttpStatusCode.OK)
            call.respond(thresholds)
        }

        post("/api/thresholds") {
            val thresholds = monitorService.getThresholds() ?: call.respond(HttpStatusCode.OK)
            call.respond(thresholds)
        }
    }
}