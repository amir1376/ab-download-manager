package ir.amirab.util

import kotlin.reflect.KProperty

data class ValueHolder<T>(
    var value: T
)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> ValueHolder<T>.setValue(thisObj: Any?, property: KProperty<*>, value: T) {
    this.value = value
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> ValueHolder<T>.getValue(thisObj: Any?, property: KProperty<*>): T = value
