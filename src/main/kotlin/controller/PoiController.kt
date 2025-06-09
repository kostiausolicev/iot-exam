package ru.guap.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import ru.guap.dto.PoiDto
import ru.guap.service.PoiService

fun Application.poiController() {
    val poiService by inject<PoiService>()
    routing {
        route("/api/poi") {
            get {
                val pois = poiService.getAll()
                call.respond(HttpStatusCode.OK, pois)
            }
            post {
                val poi = call.receive<PoiDto>()
                poiService.create(poi)
                call.respond(HttpStatusCode.Created, poi)
            }
            delete("/{name}") {
                val name = call.parameters["name"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid POI ID")
                poiService.delete(name)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}