package ir.amirab.downloader.downloaditem.http

import ir.amirab.downloader.connection.Connection
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.connection.response.expectSuccess
import ir.amirab.downloader.destination.DestWriter
import ir.amirab.downloader.exception.ServerPartIsNotTheSameAsWeExpectException
import ir.amirab.downloader.part.PartDownloader
import ir.amirab.downloader.utils.speedlimiter.SpeedLimiter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import okio.Source
import kotlin.math.min

class HttpBundlePartDownloader(
    part: HttpBundlePart,
    getDestWriter: () -> DestWriter,
    private val credentials: HttpDownloadCredentials,
    private val client: HttpDownloaderClient,
    private val speedLimiters: List<SpeedLimiter>,
) : PartDownloader<HttpBundlePart>(part, getDestWriter) {

    override fun howMuchCanRead(maxAllowed: Long): Long {
        return min(maxAllowed, (part.length - part.current).coerceAtLeast(0))
    }

    override suspend fun connectAndVerify(): Connection<HttpResponseInfo> {
        val requestedStart = part.current
        val requestedEnd = part.length - 1
        val connection = client.connect(credentials, requestedStart, requestedEnd)
        runCatching { connection.responseInfo.expectSuccess() }
            .onFailure { runCatching { connection.close() } }
            .getOrThrow()

        if (stop || !currentCoroutineContext().isActive) {
            connection.close()
            throw CancellationException()
        }

        val expectedLength = part.length - requestedStart
        val actualLength = connection.contentLength.takeIf { it >= 0 }
        val startsAtRequestedByte = connection.responseInfo.contentRange
            ?.range
            ?.first == requestedStart
        if (actualLength != expectedLength && !startsAtRequestedByte) {
            connection.close()
            throw ServerPartIsNotTheSameAsWeExpectException(
                start = requestedStart,
                end = requestedEnd,
                expectedLength = expectedLength,
                actualLength = actualLength,
            )
        }

        val limitedSource = speedLimiters.fold<SpeedLimiter, Source>(connection.source) { source, limiter ->
            limiter.source(source)
        }
        return connection.copy(source = limitedSource)
    }

    override fun onFinish() {
        if (part.isCompleted) {
            super.onFinish()
        } else {
            onCanceled(
                ServerPartIsNotTheSameAsWeExpectException(
                    start = part.current,
                    end = part.length - 1,
                    expectedLength = part.length - part.current,
                    actualLength = 0,
                )
            )
        }
    }
}
