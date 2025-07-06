package ir.amirab.util.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer


context(json: Json)
inline fun <reified T : Any> MapConfig.putEncoded(key: String, value: T) {
    putString(key, json.encodeToString(serializer<T>(), value))
}

context(json: Json)
inline fun <reified T> MapConfig.putEncodedNullable(key: ConfigKey.OfNotPrimitiveType<T>, value: T?) {
    if (value != null) {
        putString(key.keyName, json.encodeToString(serializer<T>(), value))
    } else {
        removeKey(key)
    }
}

context(_: Json)
inline fun <reified T : Any> MapConfig.putEncoded(key: ConfigKey.OfNotPrimitiveType<T>, value: T) {
    putEncoded<T>(key.keyName, value)
}

context(json: Json)
inline fun <reified T> MapConfig.getDecoded(key: String): T? {
    val str = getString(key) ?: return null
    return runCatching<T> {
        json.decodeFromString(str)
    }
        .onFailure {
            //log error
        }
        .getOrNull()
}

context(_: Json)
inline fun <reified T> MapConfig.getDecoded(key: ConfigKey.OfNotPrimitiveType<T>): T? {
    return getDecoded(key.keyName)
}

inline fun <reified T : Any> MapConfig.putNullable(key: ConfigKey.OfPrimitiveType<T>, value: T?) {
    if (value == null) {
        removeKey(key)
    } else {
        put(key, value)
    }
}
