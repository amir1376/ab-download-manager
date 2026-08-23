package ir.amirab.util.config.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import io.github.amir1376.schemakt.schema.composite.TypeSafeObjectSchema
import ir.amirab.util.config.JsonObjectToMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class JsonElementSerializer(
    private val json: Json,
    private val defaultJsonElement: () -> JsonElement,
) : Serializer<JsonElement> {
    override val defaultValue: JsonElement
        get() = defaultJsonElement()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): JsonElement {
        return withContext(Dispatchers.IO) {
            try {
                json.decodeFromStream(input)
            } catch (e: SerializationException) {
                throw CorruptionException("Json is corrupted", e)
            }
        }
    }

    override suspend fun writeTo(t: JsonElement, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(json.encodeToString(t).toByteArray())
        }
    }

}

inline fun <reified T> createSchemaBasedDatastore(
    file: File,
    json: Json,
    schema: SettingsTypeSafeSchema<T>
): DataStore<T> {
    val jsonObjectToMap = JsonObjectToMap()

    val emptyJsonObjectFactory = {
        JsonObject(emptyMap())
    }

    return DataStoreFactory.create(
        serializer = JsonElementSerializer(
            json,
            defaultJsonElement = emptyJsonObjectFactory
        ).map(
            map = { element: JsonElement ->
                val jsonObject = (element as? JsonObject) ?: emptyJsonObjectFactory()
                val map = jsonObjectToMap.transformJsonObject(jsonObject)
                schema.parse(map).getOrThrow()
            },
            unMap = { value: T ->
                json.encodeToJsonElement(value)
            }
        ),
        produceFile = { file },
        corruptionHandler = ReplaceFileCorruptionHandler {
            schema.getDefaultSettings()
        },
    )
}

