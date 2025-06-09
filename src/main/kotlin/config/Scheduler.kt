package ru.guap.config

import io.github.flaxoos.ktor.server.plugins.taskscheduling.TaskScheduling
import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.database.mongoDb
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.tryGetString

fun Application.configureScheduler() {
//    install(TaskScheduling) {
//        mongoDb("my mongodb manager") {
//            databaseName = environment.config.tryGetString("db.mongo.database.name") ?: "myDatabase"
//            client = MongoClient.create("mongodb://host:port")
//        }
//    }
}