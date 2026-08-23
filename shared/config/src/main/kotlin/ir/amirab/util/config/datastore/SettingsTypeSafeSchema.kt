package ir.amirab.util.config.datastore

import io.github.amir1376.schemakt.Schema
import io.github.amir1376.schemakt.ValidationResult
import io.github.amir1376.schemakt.schema.composite.TypeSafeObjectSchema

/**
 * the [TypeSafeObjectSchema] must catch all props!
 */
class SettingsTypeSafeSchema<T> internal constructor(
    private val typeSafeObjectSchema: TypeSafeObjectSchema<T>
) : Schema<T> {
    override fun parse(value: Any?): ValidationResult<T> {
        return typeSafeObjectSchema.parse(value)
    }

    fun getDefaultSettings(): T {
        try {
            return parse(emptyMap<String, Any?>()).getOrThrow()
        } catch (e: Exception) {
            throw IllegalStateException(
                "in settings schema, all fields in schema should have a catch",
                e
            )
        }
    }
}

fun <T> TypeSafeObjectSchema<T>.asSettingsSchema(
    checkDefaultEagerly: Boolean = false,
): SettingsTypeSafeSchema<T> {
    val schema = SettingsTypeSafeSchema(this)
    if (checkDefaultEagerly) {
        schema.getDefaultSettings()
    }
    return schema
}
