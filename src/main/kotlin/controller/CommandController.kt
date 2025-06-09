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
import ru.guap.dto.SendCommandDto
import ru.guap.service.RemoteTerminalService

fun Application.commandController() {
    val remoteTerminalService by inject<RemoteTerminalService>()
    routing {
        route("/api/commands") {
            post {
                val command = call.receive<SendCommandDto>()
                remoteTerminalService.addCommand(command)
                call.respond(HttpStatusCode.OK)
            }

            get {
                val commands = remoteTerminalService.getCommands()
                call.respond(HttpStatusCode.OK, commands)
            }

            delete("/{n}") {
                val n = call.request.pathVariables["n"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid command number")
                remoteTerminalService.removeCommand(n)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}