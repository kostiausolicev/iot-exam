package ru.guap.service

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import ru.guap.config.Collections
import ru.guap.dto.PoiDto

class PoiService(
    private val mongoDatabase: MongoDatabase
) {
    suspend fun create(poi: PoiDto) {
        mongoDatabase.getCollection<PoiDto>(Collections.POI.collectionName)
            .insertOne(poi)
    }

    suspend fun getAll(): List<PoiDto> {
        return mongoDatabase.getCollection<PoiDto>(Collections.POI.collectionName)
            .find()
            .toList()
    }

    suspend fun delete(name: String) {
        mongoDatabase.getCollection<PoiDto>(Collections.POI.collectionName)
            .deleteOne(Filters.eq("name", name))
    }
}