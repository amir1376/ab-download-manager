package com.abdownloadmanager.desktop.nativemessaging.host

import ir.amirab.util.logger.thisLogger
import kotlinx.serialization.json.Json

interface NativeMessagingMessageHandler {
    fun accept(request: NativeMessagingMessage.Content): Boolean

    suspend fun handle(
        request: NativeMessagingMessage.Content
    ): NativeMessagingMessage.Content
}

class NativeMessagingRouterNotFound(action: String) : RuntimeException(
    "Handler for '$action' not found"
)

class NativeMessagingRouter(
    private val json: Json,
    val handlers: List<NativeMessagingMessageHandler>
) {
    val logger = thisLogger()
    suspend fun handle(
        request: NativeMessagingMessage
    ): NativeMessagingMessage {
        val handler = handlers.firstOrNull {
            it.accept(request.content)
        }
        if (handler == null) {
            throw NativeMessagingRouterNotFound(request.content.action ?: "<null>")
        }
        return NativeMessagingMessage(
            id = request.id,
            content = context(json) {
                try {
                    handler.handle(request.content)
                } catch (e: Exception) {
                    logger.e(e) { "Error while handling native messaging request" }
                    NativeMessagingMessage.Content.error(throwable = e)
                }
            }
        )
    }
}
