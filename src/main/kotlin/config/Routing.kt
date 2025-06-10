package ru.guap.config

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import org.koin.ktor.ext.inject
import ru.guap.controller.alertController
import ru.guap.controller.commandController
import ru.guap.controller.dataController
import ru.guap.controller.deviceController
import ru.guap.controller.poiController
import ru.guap.controller.statusesController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import kotlin.time.Duration.Companion.seconds

fun Application.configureRouting() {
    val mongoDatabase by inject<MongoDatabase>()
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(text = "400: $cause", status = HttpStatusCode.BadRequest)
        }
        exception<Throwable> { call, cause ->
            mongoDatabase.getCollection<Map<String, String?>>(Collections.LOGS.collectionName)
                .insertOne(mapOf(LocalDateTime.now().format(ISO_LOCAL_DATE) to cause.message))
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
    deviceController()
    dataController()
    alertController()
}
