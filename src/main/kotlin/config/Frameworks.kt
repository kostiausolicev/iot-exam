package ru.guap.config

import com.mongodb.client.MongoDatabase
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import ru.guap.service.RemoteTerminalService

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<MongoDatabase> {
                connectToMongoDB()
            }
            single<RemoteTerminalService> {
                RemoteTerminalService(get())
            }
        })
    }
}
