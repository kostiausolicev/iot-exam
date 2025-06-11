package ru.guap.config

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import ru.guap.service.MonitorService
import ru.guap.service.PoiService
import ru.guap.service.RemoteTerminalService
import ru.guap.thing.Device
import ru.guap.thing.robot.GrabRobot
import ru.guap.thing.robot.VacuumRobot
import ru.guap.thing.smart.lamp.SmartLamp

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
                RemoteTerminalService(get(), get())
            }
            single<MonitorService> {
                MonitorService(get(), get())
            }
            single<PoiService> {
                PoiService(get())
            }
            single<MutableList<out Device>> {
                mutableListOf(
                    SmartLamp(id = 1),
                    GrabRobot(id = 2),
                    VacuumRobot(id = 3),
                )
            }
        })
    }
}
