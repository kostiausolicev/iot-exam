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
import ru.guap.dto.LogDto
import ru.guap.thing.Device
import java.time.LocalDateTime

class MonitorService(
    private val mongoDatabase: MongoDatabase,
    private val devices: MutableList<out Device>
) {
    private var lastLog: LogDto? = null

    suspend fun clearLogs() {
        mongoDatabase.getCollection<LogDto>(Collections.LOGS.collectionName)
            .deleteMany(Filters.lte("timestamp", LocalDateTime.now()))
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
            emit(data)
            delay(2000)
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
}