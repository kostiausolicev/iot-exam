package ru.guap.config

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import org.bson.BsonDateTime
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

object LocalDateSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        when (encoder) {
            is BsonEncoder -> {
                // Конвертируем LocalDateTime в Instant (в UTC) и берем миллисекунды
                val instant = value.toInstant(ZoneOffset.UTC)
                encoder.encodeBsonValue(BsonDateTime(instant.toEpochMilli()))
            }
            else -> encoder.encodeString(value.toString())
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return when (decoder) {
            is BsonDecoder -> {
                // Получаем миллисекунды из BsonDateTime и конвертируем в LocalDateTime
                val millis = decoder.decodeBsonValue().asDateTime().value
                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDateTime()
            }
            else -> LocalDateTime.parse(decoder.decodeString())
        }
    }
}

object AnyMapSerializer : KSerializer<Map<String, Any?>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Map<String, Any?>")

    override fun serialize(encoder: Encoder, value: Map<String, Any?>) {
        require(encoder is JsonEncoder) { "AnyMapSerializer supports only JSON encoding" }
        encoder.encodeJsonElement(buildJsonObject {
            value.forEach { (key, v) ->
                put(key, when (v) {
                    is String -> JsonPrimitive(v)
                    is Int -> JsonPrimitive(v)
                    is Double -> JsonPrimitive(v)
                    is Boolean -> JsonPrimitive(v)
                    is Map<*, *> -> Json.encodeToJsonElement(AnyMapSerializer, v as Map<String, Any?>)
                    is List<*> -> JsonArray(v.map { item ->
                        when (item) {
                            is String -> JsonPrimitive(item)
                            is Int -> JsonPrimitive(item)
                            is Double -> JsonPrimitive(item)
                            is Boolean -> JsonPrimitive(item)
                            is Map<*, *> -> Json.encodeToJsonElement(AnyMapSerializer, item as Map<String, Any?>)
                            is List<*> -> Json.encodeToJsonElement(ListSerializer(AnySerializer), item)
                            null -> JsonNull
                            else -> throw IllegalArgumentException("Unsupported type for key $key: ${item::class}")
                        }
                    })
                    null -> JsonNull
                    else -> throw IllegalArgumentException("Unsupported type for key $key: ${v::class}")
                })
            }
        })
    }

    override fun deserialize(decoder: Decoder): Map<String, Any?> {
        require(decoder is JsonDecoder) { "AnyMapSerializer supports only JSON decoding" }
        val element = decoder.decodeJsonElement()
        check(element is JsonObject) { "Expected JsonObject, found ${element::class}" }
        return element.mapValues { (_, v) ->
            when (v) {
                is JsonPrimitive -> when {
                    v.isString -> v.content
                    v.booleanOrNull != null -> v.boolean
                    v.intOrNull != null -> v.int
                    v.doubleOrNull != null -> v.double
                    else -> null
                }
                is JsonObject -> Json.decodeFromJsonElement(AnyMapSerializer, v)
                is JsonArray -> v.map { item ->
                    when (item) {
                        is JsonPrimitive -> when {
                            item.isString -> item.content
                            item.booleanOrNull != null -> item.boolean
                            item.intOrNull != null -> item.int
                            item.doubleOrNull != null -> item.double
                            else -> null
                        }
                        is JsonObject -> Json.decodeFromJsonElement(AnyMapSerializer, item)
                        is JsonArray -> Json.decodeFromJsonElement(ListSerializer(AnySerializer), item)
                        else -> throw IllegalArgumentException("Unsupported JSON element: $item")
                    }
                }
                else -> throw IllegalArgumentException("Unsupported JSON element: $v")
            }
        }
    }
}

object AnySerializer : KSerializer<Any?> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Any")

    override fun serialize(encoder: Encoder, value: Any?) {
        when (value) {
            is String -> encoder.encodeString(value)
            is Int -> encoder.encodeInt(value)
            is Double -> encoder.encodeDouble(value)
            is Boolean -> encoder.encodeBoolean(value)
            is Map<*, *> -> AnyMapSerializer.serialize(encoder, value as Map<String, Any?>)
            is List<*> -> ListSerializer(AnySerializer).serialize(encoder, value)
            null -> encoder.encodeNull()
            else -> throw IllegalArgumentException("Unsupported type: ${value::class}")
        }
    }

    override fun deserialize(decoder: Decoder): Any? {
        val jsonDecoder = decoder as? JsonDecoder ?: throw IllegalArgumentException("AnySerializer supports only JSON decoding")
        val element = jsonDecoder.decodeJsonElement()
        return when (element) {
            is JsonPrimitive -> when {
                element.isString -> element.content
                element.booleanOrNull != null -> element.boolean
                element.intOrNull != null -> element.int
                element.doubleOrNull != null -> element.double
                else -> null
            }
            is JsonObject -> Json.decodeFromJsonElement(AnyMapSerializer, element)
            is JsonArray -> Json.decodeFromJsonElement(ListSerializer(AnySerializer), element)
            else -> throw IllegalArgumentException("Unsupported JSON element: $element")
        }
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true

        })
    }
}
