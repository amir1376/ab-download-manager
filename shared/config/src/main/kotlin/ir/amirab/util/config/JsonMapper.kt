package ir.amirab.util.config

import kotlinx.serialization.json.*


class JsonObjectToMap {
    private fun transform(jsonElement: JsonElement): Any? {
        return when (jsonElement) {
            is JsonArray -> transformJsonArray(jsonElement)
            is JsonObject -> transformJsonObject(jsonElement)
            is JsonPrimitive -> transformJsonPrimitive(jsonElement)
        }
    }

    fun transformJsonObject(jsonObject: JsonObject): Map<String, Any?> {
        return jsonObject.mapValues {
            transform(it.value)
        }
    }

    fun transformJsonArray(jsonArray: JsonArray): List<Any?> {
        return jsonArray.map {
            transform(it)
        }
    }

    private fun transformJsonPrimitive(primitive: JsonPrimitive): Any? {
        if (primitive.isString) {
            return primitive.content
        }
        if (primitive == JsonNull) return null
        //bool or number
        primitive.booleanOrNull?.let {
            return it
        }
        if (primitive.content.contains(".")) {
            return primitive.double
        }
        return primitive.long
    }
}

class MapToJsonObject {
    private fun transformList(list: List<*>): JsonArray {
        return JsonArray(
            list.map {
                transform(it)
            }
        )
    }

    fun transformMap(map: Map<String, Any?>): JsonObject {
        return map.mapValues {
            transform(it.value)
        }.let {
            JsonObject(it)
        }
    }

    private fun transform(value: Any?): JsonElement {
        return when (value) {
            null -> JsonNull
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                transformMap(value as Map<String, Any>)
            }

            is List<*> -> {
                transformList(value)
            }

            else -> transformPrimitive(value)
        }
    }

    private fun transformPrimitive(value: Any): JsonPrimitive {
        return when (value) {
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            else -> error("not supported this type ${value::class.qualifiedName}")
        }
    }
}
