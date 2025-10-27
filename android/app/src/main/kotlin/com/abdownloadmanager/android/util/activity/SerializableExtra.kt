package com.abdownloadmanager.android.util.activity

import android.content.Intent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer


context(json: Json)
fun <T> Intent.getSerializedExtra(name: String, serializer: KSerializer<T>): T? {
    return getStringExtra(name)?.let {
        json.decodeFromString(serializer, it)
    }
}

context(json: Json)
fun <T> Intent.putSerializedExtra(name: String, data: T, serializer: KSerializer<T>) {
    putExtra(
        name,
        json.encodeToString(serializer, data)
    )
}

context(json: Json)
inline fun <reified T> Intent.getSerializedExtra(name: String): T? {
    return getSerializedExtra(name, serializer())
}

context(json: Json)
inline fun <reified T> Intent.putSerializedExtra(name: String, data: T) {
    putSerializedExtra(name, data, serializer())
}
