package ru.guap.config

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.log
import io.ktor.server.config.tryGetString
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject

enum class Collections(val collectionName: String) {
    COMMANDS("commands"),
    POI("poi"),
    LOGS("logs"),
    METRIC("metrics")
}

fun Application.getMongoDbClient(): MongoClient {
    val user = environment.config.tryGetString("db.mongo.user")
    val password = environment.config.tryGetString("db.mongo.password")
    val host = environment.config.tryGetString("db.mongo.host") ?: "127.0.0.1"
    val port = environment.config.tryGetString("db.mongo.port") ?: "27017"
    val maxPoolSize = environment.config.tryGetString("db.mongo.maxPoolSize")?.toInt() ?: 20

    val credentials = user?.let { userVal -> password?.let { passwordVal -> "$userVal:$passwordVal@" } }.orEmpty()
    val uri = "mongodb://$credentials$host:$port/?maxPoolSize=$maxPoolSize&w=majority"

    return MongoClient.create(uri)
}

fun Application.connectToMongoDB(): MongoDatabase {
    val databaseName = environment.config.tryGetString("db.mongo.database.name") ?: "myDatabase"
    val mongoClient by inject<MongoClient>()

    val database = mongoClient.getDatabase(databaseName)

    runBlocking {
        Collections.entries.forEach { collection ->
            database.createCollection(collection.collectionName)
        }
    }

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
        log.info("MongoDB client closed")
    }

    return database
}
