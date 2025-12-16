package ir.amirab.downloader.downloaditem.hls

import io.lindstrom.m3u8.model.KeyMethod
import io.lindstrom.m3u8.model.MediaPlaylist
import io.lindstrom.m3u8.parser.MediaPlaylistParser
import io.lindstrom.m3u8.parser.ParsingMode
import ir.amirab.downloader.connection.Connection
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.connection.response.expectSuccess
import ir.amirab.downloader.utils.FileNameUtil
import ir.amirab.util.HttpUrlUtils
import okio.buffer
import java.io.IOException

data class HLSResponseInfo(
    val httpResponseInfo: HttpResponseInfo,
    val hlsManifest: MediaPlaylist
) : IResponseInfo {
    val name = httpResponseInfo.fileName
    val duration = hlsManifest.mediaSegments()
        .sumOf { it.duration() }
        .takeIf { it > 0 }

    override val isSuccessFul: Boolean
        get() = httpResponseInfo.isSuccessFul
    override val requiresAuth: Boolean
        get() = httpResponseInfo.requireBasicAuth
    override val requireBasicAuth: Boolean
        get() = httpResponseInfo.requireBasicAuth
    override val resumeSupport: Boolean
        get() = true

    companion object {
        fun fromConnection(connection: Connection<HttpResponseInfo>): HLSResponseInfo {
            expectSuccess(connection)
            val data = connection.source.buffer().use { it.readUtf8() }
            val playlist = try {
                parseHLSAsMediaPlaylist(data)
            } catch (e: Exception) {
                throw BadHLSResponseException("can't parse HLS playlist", e)
            }
            val mediaSegments = playlist.mediaSegments()
            if (mediaSegments.isEmpty()) {
                throw UnsupportedOperationException(
                    "playlist has no segments"
                )
            }
            val firstSegmentExtension = HttpUrlUtils
                .createURL(connection.responseInfo.requestUrl)
                .resolve(mediaSegments[0].uri())?.toString()
                ?.let(HttpUrlUtils::extractNameFromLink)
                ?.let(FileNameUtil::getExtensionOrNull)
                ?.lowercase()
            if (firstSegmentExtension != "ts") {
                throw UnsupportedOperationException(
                    "Only HLS .ts segments supported at the moment, but '$firstSegmentExtension' provided"
                )
            }
            if (isMediaPlayListEncrypted(playlist)) {
                throw UnsupportedOperationException(
                    "Encrypted HLS playlists are not supported"
                )
            }
            return HLSResponseInfo(
                connection.responseInfo,
                playlist,
            )
        }

        private fun parseHLSAsMediaPlaylist(data: String): MediaPlaylist {
            val playlistParser = MediaPlaylistParser(ParsingMode.LENIENT)
            return playlistParser.readPlaylist(data)
        }

        private val HALS_POSSIBLE_HEADERS = listOf(
            "application/x-mpegurl",
            "application/vnd.apple.mpegurl",
        )

        /**
         * if no hls content-size received we check this
         */
        private const val MAXIMUM_ALLOWED_SIZE = 2 * 1024 * 1024 // 2MiB
        private fun expectSuccess(connection: Connection<HttpResponseInfo>) {
            connection.responseInfo.expectSuccess()
            val hlsPossibleHeaders = HALS_POSSIBLE_HEADERS
            val contentType = connection.responseInfo.responseHeaders["content-type"]
            var error: String? = null
            if (contentType == null) {
                error = "no content type is provided"
            } else {
                val isHLSContentType = hlsPossibleHeaders.any { it.startsWith(contentType, ignoreCase = true) }
                if (!isHLSContentType) {
                    error = "content type is not hls compatible: $contentType"
                }
            }
            if (error != null) {
                val contentLength = connection.responseInfo.contentLength
                if (contentLength == null) {
                    error += ", and content length is unknown"
                } else {
                    if (contentLength > MAXIMUM_ALLOWED_SIZE) {
                        error += ", and returned content length is too big for hls playlist"
                    } else {
                        error = null
                    }
                }
            }
            if (error != null) {
                throw BadHLSResponseException(error)
            }
        }

        private fun isMediaPlayListEncrypted(playlist: MediaPlaylist): Boolean {
            return playlist.mediaSegments().any {
                it.segmentKeys().any { key ->
                    key.method() != KeyMethod.NONE
                }
            }
        }
    }
}

class BadHLSResponseException(
    message: String, cause: Throwable? = null
) : IOException(message, cause)
