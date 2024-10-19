package ir.amirab.util.config.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import ir.amirab.util.config.MapConfig
import ir.amirab.util.config.loadFromJson
import ir.amirab.util.config.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream


class MyConfigSerializer(
    private val  json: Json
) : Serializer<MapConfig> {
    override val defaultValue: MapConfig
        get() = MapConfig()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): MapConfig {
        return withContext(Dispatchers.IO) {
            MapConfig().apply {
                try {
                    loadFromJson(json.decodeFromStream(input))
                }catch (e:SerializationException){
                    throw CorruptionException("Json is corrupted",e)
                }
            }
        }
    }

    override suspend fun writeTo(t: MapConfig, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(json.encodeToString(t.toJson()).toByteArray())
        }
    }
}

fun createMapConfigDatastore(
    file: File,
    json: Json,
): DataStore<MapConfig> {
    return DataStoreFactory.create(
        serializer = MyConfigSerializer(json),
        produceFile = { file },
        corruptionHandler = ReplaceFileCorruptionHandler{
            MapConfig()
        },
    )
}

suspend fun DataStore<MapConfig>.edit(
    editor: (MapConfig) -> Unit,
) {
    updateData {
        val newConfig = MapConfig(it)
        editor(newConfig)
        newConfig
    }
}
