package com.abdownloadmanager.desktop.storage.base

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class KotlinSerializationDataStore<T>(
    val json: Json,
    val serializer: KSerializer<T>,
    val default: () -> T,
) : Serializer<T> {
    override val defaultValue: T get() = default()
    override suspend fun readFrom(input: InputStream): T {
        try {
            @OptIn(ExperimentalSerializationApi::class)
            return json.decodeFromStream(serializer, input)
        } catch (e: SerializationException) {
            throw CorruptionException("cant decode this input", e)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        @OptIn(ExperimentalSerializationApi::class)
        json.encodeToStream(serializer, t, output)
    }
}

inline fun <reified T> kotlinxSerializationDataStore(
    file: File,
    json: Json,
    noinline default: () -> T,
): DataStore<T> {
    return DataStoreFactory.create(
        serializer = KotlinSerializationDataStore(
            json = json,
            serializer = serializer<T>(),
            default = {
                // no data found
                default()
            }
        ),
        produceFile = { file },
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = {
                // exception thrown during decoding
                default()
            }
        ),
    )
}
