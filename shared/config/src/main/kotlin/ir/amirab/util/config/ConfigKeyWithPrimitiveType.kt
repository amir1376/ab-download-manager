package ir.amirab.util.config

sealed class ConfigKey {
    abstract val keyName: String

    class OfPrimitiveType<T> internal constructor(
        override val keyName: String,
        val primitiveType: Config.PrimitiveType<T>,
    ) : ConfigKey()

    class OfNotPrimitiveType<T>(
        override val keyName: String,
    ) : ConfigKey()
}

fun <T> keyOfEncoded(name: String) = ConfigKey.OfNotPrimitiveType<T>(
    keyName = name,
)

fun intKeyOf(name: String) = ConfigKey.OfPrimitiveType(
    keyName = name, primitiveType = Config.PrimitiveType.Int
)

fun floatKeyOf(name: String) = ConfigKey.OfPrimitiveType(
    keyName = name, primitiveType = Config.PrimitiveType.Float
)

fun longKeyOf(name: String) = ConfigKey.OfPrimitiveType(
    keyName = name, primitiveType = Config.PrimitiveType.Long
)

fun doubleKeyOf(name: String) = ConfigKey.OfPrimitiveType(
    keyName = name, primitiveType = Config.PrimitiveType.Double
)

fun booleanKeyOf(name: String) = ConfigKey.OfPrimitiveType(
    keyName = name, primitiveType = Config.PrimitiveType.Boolean
)

fun stringKeyOf(name: String) = ConfigKey.OfPrimitiveType(
    keyName = name, primitiveType = Config.PrimitiveType.String
)
