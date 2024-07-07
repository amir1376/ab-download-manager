package ir.amirab.util.config

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object ConfigToJson {
    private val nestedMapCreator get() = NestedMapCreator()
    fun fromJson(configRegistry: Config, element: JsonObject) {
        val x = JsonObjectToMap().transformJsonObject(element)
        nestedMapCreator.createFlatten(x)
            .forEach { (key, value) ->
                if (value!=null){
                    val type = Config.PrimitiveType.fromValue(value)
                    if (type!=null){
                        configRegistry.put(key,type,value)
                    }
                }
            }
    }

    fun toJson(configRegistry: Config): JsonElement {
        return MapToJsonObject()
            .transformMap(nestedMapCreator.createdNested(configRegistry.toMap()))
    }
}

fun Config.toJson(): JsonElement {
    return ConfigToJson.toJson(this)
}
fun <T : Config> T.loadFromJson(json: JsonObject): T {
    ConfigToJson.fromJson(this, json)
    return this
}