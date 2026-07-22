package com.abdownloadmanager.desktop.nativemessaging.host.stdio

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Native Messaging Protocol implementation for stdio communication.
 * 
 * The protocol uses a 4-byte unsigned integer (in native byte order) to indicate 
 * the message length, followed by the JSON message content.
 * 
 * Chrome limit: 1MB
 * Firefox limit: 4MB
 */
class StdioProtocol(
    private val input: InputStream,
    private val output: OutputStream,
) {
    companion object {
        private const val MAX_MESSAGE_SIZE = 1024 * 1024 * 4 // 4MB (Firefox limit)
        private const val MESSAGE_LENGTH_SIZE = 4
    }

    /**
     * Reads a single message from stdin.
     * 
     * @return The JSON message as a string, or null if EOF is reached
     * @throws EOFException if stream ends unexpectedly
     * @throws IllegalArgumentException if message size exceeds limit
     */
    fun readMessage(): String {
        val messageLength = readLength(input)

        // Validate message size
        if (messageLength !in 1..MAX_MESSAGE_SIZE) {
            throw IllegalArgumentException(
                "Invalid message size: $messageLength (max: $MAX_MESSAGE_SIZE)"
            )
        }

        // Read message content
        val messageBytes = ByteArray(messageLength)
        val messageBytesRead = input.readNBytes(messageBytes, 0, messageLength)

        if (messageBytesRead < messageLength) {
            throw EOFException(
                "Unexpected end of stream while reading message content " +
                        "(expected: $messageLength, got: $messageBytesRead)"
            )
        }

        return String(messageBytes, Charsets.UTF_8)
    }

    /**
     * Writes a message to stdout.
     * 
     * @param message The JSON message to send
     * @throws IllegalArgumentException if message exceeds size limit
     */
    fun writeMessage(message: String) {
        val messageBytes = message.toByteArray(Charsets.UTF_8)
        val messageLength = messageBytes.size

        // Validate message size
        if (messageLength > MAX_MESSAGE_SIZE) {
            throw IllegalArgumentException(
                "Message size exceeds limit: $messageLength (max: $MAX_MESSAGE_SIZE)"
            )
        }

        // Write 4-byte length prefix in native byte order
        val lengthBuffer = ByteBuffer.allocate(4)
            .order(ByteOrder.nativeOrder())
            .putInt(messageLength)

        output.write(lengthBuffer.array())

        // Write message content
        output.write(messageBytes)
        output.flush()
    }

    fun readLength(input: InputStream): Int {
        // Read 4-byte length prefix
        val lengthBytes = ByteArray(MESSAGE_LENGTH_SIZE)
        val bytesRead = input.readNBytes(lengthBytes, 0, 4)

        if (bytesRead == 0) {
            // EOF reached, browser disconnected
            throw NativeMessagingFinished()
        }

        if (bytesRead < 4) {
            throw EOFException("Unexpected end of stream while reading message length")
        }

        // Convert to unsigned int using native byte order
        return ByteBuffer.wrap(lengthBytes)
            .order(ByteOrder.nativeOrder())
            .int
    }
}
