package ir.amirab.util.config

import java.util.Collections
import kotlin.reflect.KType
import kotlin.reflect.typeOf





interface Config {
    fun toMap(): Map<String, Any?>

    fun <T:Any>put(key: ConfigKey.OfPrimitiveType<T>, value: T){
        put(key.keyName,key.primitiveType,value)
    }

    fun <T:Any>put(key: String,type: PrimitiveType<T>,value:T)
    fun putInt(key: String, value: Int) = put(key,PrimitiveType.Int,value)
    fun putFloat(key: String, value: Float) = put(key,PrimitiveType.Float,value)
    fun putLong(key: String, value: Long) = put(key,PrimitiveType.Long,value)
    fun putDouble(key: String, value: Double) = put(key,PrimitiveType.Double,value)
    fun putBoolean(key: String, value: Boolean) = put(key,PrimitiveType.Boolean,value)
    fun putString(key: String, value: String) = put(key,PrimitiveType.String,value)

    fun <T> get(key: String, type: PrimitiveType<T>): T?
    fun <T>get(key: ConfigKey.OfPrimitiveType<T>):T?{
        return get(key.keyName,key.primitiveType)
    }

    fun getInt(key: String): Int? = get(key, Config.PrimitiveType.Int)
    fun getFloat(key: String): Float? = get(key, Config.PrimitiveType.Float)
    fun getLong(key: String): Long? = get(key, Config.PrimitiveType.Long)
    fun getDouble(key: String): Double? = get(key, Config.PrimitiveType.Double)
    fun getBoolean(key: String): Boolean? = get(key, Config.PrimitiveType.Boolean)
    fun getString(key: String): String? = get(key,Config.PrimitiveType.String)


    fun removeKey(key: String)
    fun removeKey(key: ConfigKey) {
        removeKey(key.keyName)
    }
    sealed interface PrimitiveType<T> {
        companion object{
            fun ensureIsPrimitive(value: Any) {
                require(value is Number || value is kotlin.String || value is kotlin.Boolean) {
                    "value must be number|string|boolean was ${value::class.qualifiedName}"
                }
            }
            fun <T>fromType(type:KType):PrimitiveType<T>?{
                @Suppress("UNCHECKED_CAST")
                return when(type){
                    typeOf<kotlin.Int>() -> PrimitiveType.Int
                    typeOf<kotlin.Long>() -> PrimitiveType.Long
                    typeOf<kotlin.Float>() -> PrimitiveType.Float
                    typeOf<kotlin.Double>() -> PrimitiveType.Double
                    typeOf<kotlin.String>() -> PrimitiveType.String
                    typeOf<kotlin.Boolean>() -> PrimitiveType.Boolean
                    else -> null
                } as PrimitiveType<T>?
            }
            fun <T:Any>fromValue(value: T):PrimitiveType<T>?{
                @Suppress("UNCHECKED_CAST")
                return when(value){
                    is kotlin.Int-> PrimitiveType.Int
                    is kotlin.Long-> PrimitiveType.Long
                    is kotlin.Float-> PrimitiveType.Float
                    is kotlin.Double-> PrimitiveType.Double
                    is kotlin.String-> PrimitiveType.String
                    is kotlin.Boolean-> PrimitiveType.Boolean
                    else -> null
                } as PrimitiveType<T>?
            }
        }
        fun toType(value: Any):T?
        data object Int: PrimitiveType<kotlin.Int> {
            override fun toType(value: Any): kotlin.Int? {
                return if (value is Number){
                    value.toInt()
                }else{
                    value.toString().toIntOrNull()
                }
            }

        }
        data object Long: PrimitiveType<kotlin.Long> {
            override fun toType(value: Any): kotlin.Long? {
                return if (value is Number){
                    value.toLong()
                }else{
                    value.toString().toLongOrNull()
                }
            }
        }
        data object Float: PrimitiveType<kotlin.Float> {
            override fun toType(value: Any): kotlin.Float? {
                return if (value is Number){
                    value.toFloat()
                }else{
                    value.toString().toFloatOrNull()
                }
            }
        }
        data object Double: PrimitiveType<kotlin.Double> {
            override fun toType(value: Any): kotlin.Double? {
                return if (value is Number){
                    value.toDouble()
                }else{
                    value.toString().toDoubleOrNull()
                }
            }
        }
        data object String: PrimitiveType<kotlin.String> {
            override fun toType(value: Any): kotlin.String {
                return value.toString()
            }
        }

        data object Boolean: PrimitiveType<kotlin.Boolean> {
            override fun toType(value: Any): kotlin.Boolean? {
                return if (value is kotlin.Boolean){
                    value
                }else{
                    value.toString().toBooleanStrictOrNull()
                }
            }
        }
    }
}



class MapConfig() : Config {
    constructor(config: Config) : this() {
        this.map.putAll(config.toMap())
    }

    private val map = Collections.synchronizedMap(linkedMapOf<String, Any?>())

    override fun <T:Any> put(key: String, type: Config.PrimitiveType<T>, value: T) {
        map[key] = value
    }

    override fun <T> get(key: String, type: Config.PrimitiveType<T>): T? {
        val value = map[key] ?: return null
        Config.PrimitiveType.ensureIsPrimitive(value)
        return type.toType(value)
    }

    override fun removeKey(key: String) {
        map.remove(key)
    }

    override fun toMap(): Map<String, Any?> {
        return map.toMap()
    }

    override fun toString(): String {
        return toMap().toString()
    }

    override fun equals(other: Any?): Boolean {
        val otherMap = when(other){
            is MapConfig -> other.map
            is Config -> other.toMap()
            else -> null
        }
        return map == otherMap
    }

    override fun hashCode(): Int {
        return map.hashCode() ?: 0
    }

}
