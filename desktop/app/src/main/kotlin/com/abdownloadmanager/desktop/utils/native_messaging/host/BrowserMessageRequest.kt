package com.abdownloadmanager.desktop.utils.native_messaging.host

import com.abdownloadmanager.desktop.utils.native_messaging.host.NativeMessageResponse.Success
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrowserMessageRequest(
    val requestId: Long,
    val action: String,
    val payload: String,
)

@Serializable
sealed interface NativeMessageResponse {
    @SerialName("requestId")
    val requestId: Long // response to the specific request

    @SerialName("action")
    val action: String? // a key to the action

    @SerialName("payload")
    val payload: String? // a data that might be parsed like JSON

    @SerialName("payload")
    val message: String? // regular message

    @Serializable
    @SerialName("success")
    data class Success(
        override val requestId: Long,
        override val action: String,

        override val payload: String? = null,
        override val message: String? = null,
    ) : NativeMessageResponse {
        companion object
    }

    @Serializable
    @SerialName("error")
    data class Error(
        override val requestId: Long,
        override val message: String,

        override val action: String? = null,
        override val payload: String? = null,
    ) : NativeMessageResponse
}


fun Success.Companion.ok(requestId: Long): Success {
    return Success(
        requestId = requestId,
        action = "OK",
        payload = null,
    )
}
