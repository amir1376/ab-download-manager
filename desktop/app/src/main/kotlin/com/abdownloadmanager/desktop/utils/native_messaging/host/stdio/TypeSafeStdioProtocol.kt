package com.abdownloadmanager.desktop.utils.native_messaging.host.stdio

import com.abdownloadmanager.desktop.utils.native_messaging.host.BrowserMessageRequest
import com.abdownloadmanager.desktop.utils.native_messaging.host.NativeMessageResponse
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class TypeSafeStdioProtocol(
    private val json: Json,
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
) {
    private val actual = StdioProtocol

    fun send(content: NativeMessageResponse) {
        actual.writeMessage(outputStream, json.encodeToString(content))
    }

    fun receive(): BrowserMessageRequest? {
        val readMessage = actual.readMessage(inputStream) ?: return null
        return json.decodeFromString<BrowserMessageRequest>(readMessage)
    }
}
