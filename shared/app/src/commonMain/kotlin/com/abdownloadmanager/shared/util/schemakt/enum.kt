package com.abdownloadmanager.shared.util.schemakt

import io.github.amir1376.schemakt.S
import io.github.amir1376.schemakt.Schema
import io.github.amir1376.schemakt.schema.modifier.catch
import io.github.amir1376.schemakt.schema.modifier.transform
import io.github.amir1376.schemakt.schema.primitive.string

inline fun <reified T : Enum<T>> S.enum(): Schema<T> {
    return S.string().transform { enumValueOf<T>(it) }
}

