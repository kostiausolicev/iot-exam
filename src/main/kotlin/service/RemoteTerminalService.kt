package ru.guap.service

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import ru.guap.config.Collections
import ru.guap.dto.CommandDto
import ru.guap.dto.SendCommandDto
import ru.guap.thing.Device
import ru.guap.thing.smart.camera.SmartCamera

class RemoteTerminalService(
    private val mongoDatabase: MongoDatabase
) {
    private val devices: MutableList<out Device> = mutableListOf(
        SmartCamera(1)
    )

    suspend fun addCommand(commandDto: SendCommandDto) {
        val device = devices.find { it.id == commandDto.deviceId }
            ?: throw IllegalArgumentException("Device with id ${commandDto.deviceId} not found")
        val n = mongoDatabase.getCollection(
            Collections.COMMANDS.collectionName,
            CommandDto::class.java
        )
            .find()
            .sort(Sorts.descending(CommandDto::timestamp.name))
            .limit(1)
            .firstOrNull()
            ?.N?.plus(1) ?: 1
        val command = CommandDto(
            device = device.deviceName(),
            N = n, // Номер команды
            params = mapOf(
                "X" to commandDto.X,
                "Y" to commandDto.Y,
                "T" to commandDto.T,
                "G" to commandDto.G,
                "V" to commandDto.V,
                "lights" to commandDto.lights,
            ),
            status = "Pending",
            timestamp = java.time.LocalDateTime.now()
        )

        mongoDatabase.getCollection(
            Collections.COMMANDS.collectionName,
            CommandDto::class.java
        )
            .insertOne(command)
    }

    suspend fun removeCommand(n: Int) {
        mongoDatabase.getCollection(Collections.COMMANDS.collectionName, CommandDto::class.java)
            .deleteOne(Filters.eq("n", n))
    }

    suspend fun getCommands(): List<CommandDto> {
        return mongoDatabase.getCollection(Collections.COMMANDS.collectionName, CommandDto::class.java)
            .find()
            .toList()
    }
}