package ir.amirab.util.http4k

import fi.iki.elonen.NanoHTTPD
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Request as Http4KRequest
import org.http4k.core.Response as Http4kResponse
import org.http4k.core.Method as Http4kMethod
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import java.io.FilterInputStream
import java.io.InputStream
import kotlin.math.min

private open class LimitedInputStream(inputStream: InputStream, val maxRead: Long) : FilterInputStream(inputStream) {
    fun allowedRemaining() = maxRead - readCount

    var readCount = 0
        private set

    fun maxReached() = allowedRemaining() <= 0

    private fun reachedEnd(): Int {
        return -1
    }

    override fun read(): Int {
        if (maxReached()) {
            return reachedEnd()
        }
        readCount++
        return super.read()
    }

    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val allowedRead = allowedRemaining()
        if (allowedRead <= 0) {
            return reachedEnd()
        }
        val newLen = min(allowedRead, len.toLong())

        val result = super.read(b, off, newLen.toInt())
        if (result >= 0) {
            readCount += result
        }
        return result
    }

    override fun readAllBytes(): ByteArray {
        return readNBytes(Int.MAX_VALUE)
    }

    override fun readNBytes(len: Int): ByteArray {
        val newLen = constrainRequestedLength(len)
        if (newLen < 0) {
            return byteArrayOf()
        }
        return super.readNBytes(newLen)
    }

    fun constrainRequestedLength(len: Int): Int {
        return min(allowedRemaining(), len.toLong()).toInt()
    }

}

/**
 * Nano http give me the socket's input stream
 * so Instead of the close the underlying socket
 * I skipp the remaining body size
 * so the socket is not closed and can be reused
 */
private class NanoHttpDInputStream(
    inputStream: InputStream, length: Long
) : LimitedInputStream(inputStream, length) {
    override fun close() {
        skip(allowedRemaining())
    }
}

private class NanoHttpDForHttp4K(
    hostName: String,
    port: Int,
    private val handler: HttpHandler,
    private val isDebugMode: Boolean,
) : NanoHTTPD(hostName, port) {
    private fun IHTTPSession.toHttp4KRequest(): Http4KRequest {

        val length = (this as HTTPSession).bodySize
        // I duplicate that input stream because http4k close that but it is the underlying input stream
//        val body = ByteArrayInputStream(inputStream.readNBytes(length))
        val body = NanoHttpDInputStream(inputStream, length)
        return Http4KRequest(
            method = Http4kMethod.valueOf(method.name),
            uri = this.uri
        ).body(
            body = body,
            length = length
        )
            .headers(headers.map { it.key to it.value })
    }

    private fun Http4kResponse.toNanoHttpResponse(): Response {
        val length = this.body.length

        val bodyInputStream = this.body.stream

        val nanoResponseStatus = Response.Status.lookup(status.code)

        val response = if (length != null) {
            newFixedLengthResponse(
                nanoResponseStatus,
                null,
                bodyInputStream,
                length
            )
        } else {
            newChunkedResponse(
                nanoResponseStatus,
                null,
                bodyInputStream,
            )
        }
        headers.forEach {
            response.addHeader(it.first, it.second)
        }
        return response
    }

    override fun serve(session: IHTTPSession): Response {
        val response = kotlin.runCatching {
            session.toHttp4KRequest().use { request ->
                handler(request)
            }
        }.getOrElse { throwable ->
            val shortDescription = "${throwable::class.simpleName} ${throwable.localizedMessage}"
            val extraInfo = if (isDebugMode) {
                throwable.stackTraceToString()
            } else null
            Response(Status.INTERNAL_SERVER_ERROR)
                .body("Error $shortDescription\n$extraInfo")
        }
        return response.toNanoHttpResponse()
    }
}

private class NanoHttpServer(
    val hostName: String,
    val port: Int,
    handler: HttpHandler,
    debug: Boolean,
) : Http4kServer {
    val server = NanoHttpDForHttp4K(hostName, port, handler, debug)
    override fun port(): Int {
        return if (port > 0) port
        else server.listeningPort
    }

    override fun start(): Http4kServer = apply {
        server.start()
    }

    override fun stop(): Http4kServer = apply {
        server.stop()
    }
}

class NanoHttp(
    val hostName: String,
    val port: Int,
    val isDebugMode: Boolean = false,
) : ServerConfig {
    override fun toServer(http: HttpHandler): Http4kServer {
        return NanoHttpServer(hostName, port, http, isDebugMode)
    }
}