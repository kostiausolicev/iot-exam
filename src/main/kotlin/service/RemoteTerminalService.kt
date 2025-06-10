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
import ru.guap.dto.DeviceDto
import ru.guap.dto.LampsDTO
import ru.guap.dto.RobotStatusDTO
import ru.guap.dto.SendCommandDto
import ru.guap.dto.StatusDTO
import ru.guap.thing.Device
import ru.guap.thing.robot.GrabRobot
import ru.guap.thing.robot.Robot
import ru.guap.thing.robot.VacuumRobot
import ru.guap.thing.smart.lamp.SmartLamp

class RemoteTerminalService(
    private val mongoDatabase: MongoDatabase
) {
    private val devices: MutableList<out Device> = mutableListOf(
        SmartLamp(id = 1),
        GrabRobot(id = 2),
        VacuumRobot(id = 3),
    )

    fun getDevices(): List<DeviceDto> {
        return devices.map {
            DeviceDto(
                id = it.id,
                name = it.deviceName()
            )
        }
    }

    suspend fun executeCommand() {
        for (command in getPendingCommands()) {
            val device = devices.find { it.deviceName() == command.device && !it.running }
                ?: throw IllegalArgumentException("Device with name ${command.device} not found")
            when (device) {
                is Robot -> {
                    val x = command.params["X"].toString().toIntOrNull()
                    val y = command.params["Y"].toString().toIntOrNull()
                    if (x != null || y != null) {
                        setCommandStatus(command, "Running")
                        device.moveTo(x, y) {
                            setCommandStatus(command, "Executed")
                        }
                    }

                    val t = command.params["T"].toString().toIntOrNull()
                    if (t != null) device.turn(t)

                    val grag = if (device is GrabRobot) command.params["G"].toString().toIntOrNull()
                    else command.params["V"].toString().toIntOrNull()

                    if (grag != null) device.grab(grag == 1)
                }
                is SmartLamp -> {
                    setCommandStatus(command, "Running")
                    val lights = command.params["lights"] as List<String>
                    device.setLight(lights) {
                        setCommandStatus(command, "Executed")
                    }
                }
                else -> continue
            }
        }
    }

    private suspend fun setCommandStatus(command: CommandDto, status: String) {
        // Обновляем статус команды в базе данных
        mongoDatabase.getCollection<CommandDto>(Collections.COMMANDS.collectionName)
            .updateOne(Filters.eq("n", command.N), Updates.set("status", status))
    }

    fun getStatusFlow(): Flow<StatusDTO> = flow {
        while (true) {
            emit(getStatus())
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
                s = if (robot.running) 1 else 0
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
            } ?: LampsDTO(
                    L1 = 0,
                    L2 = 0,
                    L3 = 0,
                    L4 = 0
                )
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