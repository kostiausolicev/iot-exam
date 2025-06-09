package ru.guap.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.log
import io.ktor.server.config.tryGetString
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider

enum class Collections(val collectionName: String) {
    COMMANDS("commands"),
    LOGS("logs")
}

fun Application.connectToMongoDB(): MongoDatabase {
    val user = environment.config.tryGetString("db.mongo.user")
    val password = environment.config.tryGetString("db.mongo.password")
    val host = environment.config.tryGetString("db.mongo.host") ?: "127.0.0.1"
    val port = environment.config.tryGetString("db.mongo.port") ?: "27017"
    val maxPoolSize = environment.config.tryGetString("db.mongo.maxPoolSize")?.toInt() ?: 20
    val databaseName = environment.config.tryGetString("db.mongo.database.name") ?: "myDatabase"

    val credentials = user?.let { userVal -> password?.let { passwordVal -> "$userVal:$passwordVal@" } }.orEmpty()
    val uri = "mongodb://$credentials$host:$port/?maxPoolSize=$maxPoolSize&w=majority"

    val codecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(
            PojoCodecProvider.builder()
                .automatic(true)
                .build()
        )
    )

    val mongoClient = MongoClients.create(
        MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(uri))
            .codecRegistry(codecRegistry)
            .build()
    )

    val database = mongoClient.getDatabase(databaseName)

    // Проверка и создание коллекций, если они не существуют
    val existingCollections = database.listCollectionNames().toList()
    if (Collections.COMMANDS.collectionName !in existingCollections) {
        database.createCollection(Collections.COMMANDS.collectionName)
        log.info("Created collection: ${Collections.COMMANDS.collectionName}")
    }
    if (Collections.LOGS.collectionName !in existingCollections) {
        database.createCollection(Collections.LOGS.collectionName)
        log.info("Created collection: ${Collections.LOGS.collectionName}")
    }

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
        log.info("MongoDB client closed")
    }

    return database
}
