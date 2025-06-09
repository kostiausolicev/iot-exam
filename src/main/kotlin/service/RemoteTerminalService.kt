package ru.guap.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import ru.guap.config.Collections
import ru.guap.dto.CommandDto
import ru.guap.dto.LampsDTO
import ru.guap.dto.RobotStatusDTO
import ru.guap.dto.SendCommandDto
import ru.guap.dto.StatusDTO
import ru.guap.thing.Device
import ru.guap.thing.robot.Robot
import ru.guap.thing.smart.lamp.SmartLamp

class RemoteTerminalService(
    private val mongoDatabase: MongoDatabase
) {
    private val devices: MutableList<out Device> = mutableListOf(
        SmartLamp(id = 1)
    )

    suspend fun executeCommand() {
        getPendingCommands().forEach { command ->
            val device = devices.find { it.deviceName() == command.device }
                ?: throw IllegalArgumentException("Device with name ${command.device} not found")
            when (device) {
                is SmartLamp -> {
                    val lights = command.params["lights"] as List<String>
                    device.setLight(lights) {
                        // Обновляем статус команды в базе данных
                        mongoDatabase.getCollection<CommandDto>(Collections.COMMANDS.collectionName)
                            .updateOne(Filters.eq("n", command.N), Updates.set("status", "Running"))
                    }
                }
            }
            // Обновляем статус команды в базе данных
            mongoDatabase.getCollection<CommandDto>(Collections.COMMANDS.collectionName)
                .updateOne(Filters.eq("n", command.N), Updates.set("status", "Executed"))
        }
    }

    suspend fun getStatusFlow(): Flow<StatusDTO> = flow {
        while (true) {
            val robots = devices.filterIsInstance<Robot>().map { robot ->
                RobotStatusDTO(
                    name = robot.deviceName(),
                    X = robot.X,
                    Y = robot.Y,
                    T = robot.T,
                    s = if (robot.isConnected()) 1 else 0
                )
            }
            val lamps = devices.filterIsInstance<SmartLamp>().firstOrNull()?.getLights()
                ?.map { if (it) 1 else 0 }
                ?.let { lights ->
                    LampsDTO(
                        L1 = lights.getOrNull(0) ?: 0,
                        L2 = lights.getOrNull(1) ?: 0,
                        L3 = lights.getOrNull(2) ?: 0,
                        L4 = lights.getOrNull(3) ?: 0
                    )
                }
            emit(StatusDTO(robots, lamps))
            delay(5000) // Обновлять каждые 5 секунд
        }
    }

    fun getStatus(): StatusDTO {
        val robots = devices.filterIsInstance<Robot>().map { robot ->
            RobotStatusDTO(
                name = robot.deviceName(),
                X = robot.X,
                Y = robot.Y,
                T = robot.T,
                s = if (robot.isConnected()) 1 else 0
            )
        }
        val lamps = devices.filterIsInstance<SmartLamp>().firstOrNull()?.getLights()
            ?.map { if (it) 1 else 0 }
            ?.let { lights ->
                LampsDTO(
                    L1 = lights.getOrNull(0) ?: 0,
                    L2 = lights.getOrNull(1) ?: 0,
                    L3 = lights.getOrNull(2) ?: 0,
                    L4 = lights.getOrNull(3) ?: 0
                )
            }
        return StatusDTO(robots, lamps)
    }

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
        mongoDatabase.getCollection<CommandDto>(Collections.COMMANDS.collectionName)
            .deleteOne(Filters.eq("n", n))
    }

    suspend fun getCommands(): List<CommandDto> {
        return mongoDatabase.getCollection(Collections.COMMANDS.collectionName, CommandDto::class.java)
            .find()
            .toList()
    }

    private suspend fun getPendingCommands(): List<CommandDto> {
        return mongoDatabase.getCollection(Collections.COMMANDS.collectionName, CommandDto::class.java)
            .find()
            .filter(Filters.eq("status", "Pending"))
            .toList()
    }
}