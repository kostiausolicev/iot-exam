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
import ru.guap.thing.Device

class MonitorService(
    private val mongoDatabase: MongoDatabase,
    private val devices: MutableList<out Device>
) {

    fun getDataFlow() = flow {
        while (true) {
            val data = devices.map { device ->
                getDate(device)
            }
            emit(data)
            delay(2000)
        }
    }

    suspend fun getDate(device: Device): DataDto {
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