package ru.guap.config

import com.mongodb.kotlin.client.coroutine.MongoClient
import io.github.flaxoos.ktor.server.plugins.taskscheduling.TaskScheduling
import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.database.mongoDb
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.tryGetString
import org.koin.ktor.ext.inject
import ru.guap.service.RemoteTerminalService

fun Application.configureScheduler() {
    val remoteTerminalService by inject<RemoteTerminalService>()
    val mongoClient by inject<MongoClient>()
    install(TaskScheduling) {
        mongoDb("my mongodb manager") {
            databaseName = environment.config.tryGetString("db.mongo.database.name") ?: "myDatabase"
            client = mongoClient
        }
        task("my mongodb manager") {
            name = "execute command"
            task = { taskExecutionTime ->
                remoteTerminalService.executeCommand()
            }
            kronSchedule = {
                minutes {
                    from(0).every(1)
                }
            }
            concurrency = 1
        }
    }
}