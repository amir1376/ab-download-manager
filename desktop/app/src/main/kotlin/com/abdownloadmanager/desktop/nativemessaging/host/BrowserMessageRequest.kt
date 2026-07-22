package com.abdownloadmanager.desktop.nativemessaging.host

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

const val BROWSER_ID_PREFIX = "B_"
const val NATIVE_ID_PREFIX = "N_"

@Serializable
data class NativeMessagingMessage(
    val id: Id,
    val content: Content
) {
    typealias Id = String

    fun Id.byBrowser() = startsWith(BROWSER_ID_PREFIX)
    fun Id.byNative() = startsWith(NATIVE_ID_PREFIX)

    @Serializable
    data class Content(
        val action: String?, // in case that we have action it means this is a request
        val isError: Boolean,
        val payload: String,
    ) {
        companion object {
            const val TYPE_ERROR = "error"

            context(json: Json)
            inline fun <reified T> create(
                payload: T,
                isError: Boolean = false,
                action: String? = null,
            ) = Content(
                action = action,
                isError = isError,
                payload = json.encodeToString(payload)
            )
        }
    }
}

@Serializable
data class ErrorPayload(
    val errorType: String? = null,
    val message: String? = null,
)

fun NativeMessagingMessage.Companion.generateId(): NativeMessagingMessage.Id {
    return NATIVE_ID_PREFIX + UUID.randomUUID().toString()
}

context(json: Json)
fun NativeMessagingMessage.Content.Companion.boolean(
    value: Boolean
) = create(
    payload = value
)

context(json: Json)
fun NativeMessagingMessage.Content.Companion.error(
    errorType: String? = null,
    message: String? = null
) = create(
    isError = true,
    payload = ErrorPayload(
        errorType = errorType,
        message = message,
    ),
)

context(json: Json)
fun NativeMessagingMessage.Content.Companion.error(
    throwable: Throwable
): NativeMessagingMessage.Content {
    return error(
        errorType = throwable::class.qualifiedName,
        message = throwable.localizedMessage,
    )
}
