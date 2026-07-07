package com.abdownloadmanager.desktop.utils.native_messaging.host

interface NativeMessagingMessageHandler {
    fun accept(request: BrowserMessageRequest): Boolean
    suspend fun handle(
        request: BrowserMessageRequest
    ): NativeMessageResponse
}

class NativeMessagingRouter(
    val handlers: List<NativeMessagingMessageHandler>
) {
    suspend fun handle(
        request: BrowserMessageRequest
    ): NativeMessageResponse {
        val handler = handlers.firstOrNull {
            it.accept(request)
        }
        if (handler == null) {
            return NativeMessageResponse.Error(
                requestId = request.requestId,
                payload = "404",
                message = "Handler not found"
            )
        }
        return try {
            handler.handle(request)
        } catch (e: Exception) {
            NativeMessageResponse.Error(
                requestId = request.requestId,
                payload = "500",
                message = "can't handle the request: (${request.action}) because: ${e.message}"
            )
        }
    }
}
