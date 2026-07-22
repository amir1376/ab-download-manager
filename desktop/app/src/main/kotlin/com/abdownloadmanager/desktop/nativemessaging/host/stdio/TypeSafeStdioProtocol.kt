package com.abdownloadmanager.desktop.nativemessaging.host.stdio

import com.abdownloadmanager.desktop.nativemessaging.host.NativeMessagingMessage
import kotlinx.serialization.json.Json
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

class TypeSafeStdioProtocol(
    private val json: Json,
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
) {
    private val actual = StdioProtocol(inputStream, outputStream)

    fun send(content: NativeMessagingMessage) {
        actual.writeMessage(json.encodeToString(content))
    }

    fun receive(): NativeMessagingMessage {
        val readMessage = actual.readMessage()
        return json.decodeFromString<NativeMessagingMessage>(readMessage)
    }
}
