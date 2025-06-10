package ru.guap.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import ru.guap.config.Collections
import ru.guap.dto.CommandDto
import ru.guap.dto.DataDto
import ru.guap.dto.DataPhysDto
import ru.guap.dto.FullDataDto
import ru.guap.dto.LogDto
import ru.guap.dto.ThresholdsDto
import ru.guap.thing.Device
import ru.guap.thing.robot.GrabRobot
import ru.guap.thing.robot.Robot
import ru.guap.thing.robot.VacuumRobot
import java.time.LocalDateTime

class MonitorService(
    private val mongoDatabase: MongoDatabase,
    private val devices: MutableList<out Device>
) {
    companion object {
        const val UPDATE_INTERVAL = 5_000L
    }

    private var lastLog: LogDto? = null

    suspend fun clearLogs() {
        mongoDatabase.getCollection<LogDto>(Collections.LOGS.collectionName)
            .deleteMany(Filters.lte("timestamp", LocalDateTime.now()))
    }

    fun getThresholds(): ThresholdsDto? {
        return devices.filterIsInstance<GrabRobot>().map {
            it.getThresholds()
        }.firstOrNull() ?: devices.filterIsInstance<VacuumRobot>().map {
            it.getThresholds()
        }.firstOrNull()
    }

    fun saveThresholds(dto: ThresholdsDto) {
        devices.filterIsInstance<Robot>().forEach { robot ->
            robot.saveThresholds(dto)
        }
    }

    fun getLogsFlow() = flow {
        while (true) {
            val log = getLog()
            if (log != null && log != lastLog) {
                emit(log)
                lastLog = log
            }
        }
    }

    fun getDataFlow() = flow {
        while (true) {
            val data = devices.map { device ->
                getDate(device)
            }
            val dataPhys = devices.map { device ->
                getPhysData(device)
            }
            emit(FullDataDto(
                data,
                dataPhys,
            ))
            delay(UPDATE_INTERVAL)
        }
    }

    private suspend fun getLog(): LogDto? {
        return mongoDatabase.getCollection<LogDto>(Collections.LOGS.collectionName)
            .find()
            .sort(Sorts.descending("timestamp"))
            .limit(1)
            .firstOrNull()
    }

    private suspend fun getDate(device: Device): DataDto {
        val lastCommand = mongoDatabase.getCollection<CommandDto>(Collections.COMMANDS.collectionName)
            .find(
                Filters.and(
                    Filters.eq("deviceId", device.id),
                    Filters.eq("status", "Executed")
                )
            ).sort(Sorts.descending("timestamp"))
            .limit(1)
            .firstOrNull()
            ?.N ?: -1

        return device.toDataDto(lastCommand)
    }

    private suspend fun getPhysData(device: Device): DataPhysDto {
        val lastCommand = mongoDatabase.getCollection<CommandDto>(Collections.COMMANDS.collectionName)
            .find(
                Filters.and(
                    Filters.eq("deviceId", device.id),
                    Filters.eq("status", "Executed")
                )
            ).sort(Sorts.descending("timestamp"))
            .limit(1)
            .firstOrNull()
            ?.N ?: -1

        val data = device.toDataPsycDto(lastCommand)
        mongoDatabase.getCollection<DataPhysDto>(Collections.METRIC.collectionName)
            .insertOne(data)
        return data
    }
}