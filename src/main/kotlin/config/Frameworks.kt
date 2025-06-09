package ru.guap.config

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import ru.guap.service.PoiService
import ru.guap.service.RemoteTerminalService

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<MongoClient> {
                getMongoDbClient()
            }
            single<MongoDatabase> {
                connectToMongoDB()
            }
            single<RemoteTerminalService> {
                RemoteTerminalService(get())
            }
            single<PoiService> {
                PoiService(get())
            }
        })
    }
}
