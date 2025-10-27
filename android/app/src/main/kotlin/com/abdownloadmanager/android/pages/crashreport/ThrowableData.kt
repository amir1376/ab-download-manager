package com.abdownloadmanager.android.pages.crashreport

import kotlinx.serialization.Serializable

@Serializable
data class ThrowableData(
    val title: String,
    val stacktrace: String,
) {
    companion object {
        fun fromThrowable(throwable: Throwable): ThrowableData {
            val title = throwable.localizedMessage ?: throwable.javaClass.simpleName ?: "Unknown error"
            val stacktrace = throwable.stackTraceToString().replace("\t", "    ")
            return ThrowableData(title, stacktrace)
        }
    }
}
