package ru.guap.config

import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureHTTP() {
    install(CORS) {
        anyMethod()
        allowHeader(HttpHeaders.ContentType)
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
}
