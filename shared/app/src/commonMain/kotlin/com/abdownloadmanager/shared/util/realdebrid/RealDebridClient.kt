package com.abdownloadmanager.shared.util.realdebrid

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class RealDebridException(
    message: String,
    val errorCode: Int? = null,
) : Exception(message)

@Serializable
data class RealDebridUnrestrictedLink(
    val download: String = "",
    val filename: String? = null,
    val filesize: Long = 0,
    val host: String = "",
)

@Serializable
data class RealDebridAddedTorrent(
    val id: String = "",
    val uri: String = "",
)

@Serializable
data class RealDebridTorrentInfo(
    val id: String = "",
    val filename: String = "",
    val status: String = "",
    val progress: Int = 0,
    val bytes: Long = 0,
    val links: List<String> = emptyList(),
    val files: List<RealDebridTorrentFile> = emptyList(),
)

@Serializable
data class RealDebridTorrentFile(
    val id: Int = 0,
    val path: String = "",
    val bytes: Long = 0,
    val selected: Int = 0,
)

@Serializable
private data class RealDebridErrorBody(
    val error: String = "",
    val error_code: Int? = null,
)

/**
 * Minimal client for https://api.real-debrid.com/rest/1.0
 * The API token is passed on each call so that changing it in the settings
 * takes effect immediately without recreating the client.
 */
class RealDebridClient(
    private val client: OkHttpClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun url(path: String) = "$BASE_URL$path"

    private fun requestBuilder(token: String, path: String): Request.Builder {
        return Request.Builder()
            .url(url(path))
            .header("Authorization", "Bearer ${token.trim()}")
    }

    private suspend fun execute(request: Request): String = withContext(Dispatchers.IO) {
        val response = client.newCall(request).execute()
        val body = response.body.string()
        response.close()
        if (!response.isSuccessful) {
            throw body.toRealDebridError(response.code)
        }
        body
    }

    private fun String.toRealDebridError(httpCode: Int): RealDebridException {
        val errorBody = runCatching {
            json.decodeFromString(RealDebridErrorBody.serializer(), this)
        }.getOrNull()
        val error = errorBody?.error?.takeIf { it.isNotBlank() }
        val message = when (error) {
            "bad_token" -> "Real-Debrid API token is invalid or has expired"
            "unsupported_hoster" -> "Real-Debrid does not support this host"
            "unavailable_file" -> "This file is not available on Real-Debrid anymore"
            "too_many_requests" -> "Too many Real-Debrid requests, try again later"
            null -> "Real-Debrid request failed (HTTP $httpCode)"
            else -> "Real-Debrid error: $error"
        }
        return RealDebridException(message, errorBody?.error_code)
    }

    suspend fun unrestrictLink(
        token: String,
        link: String,
    ): RealDebridUnrestrictedLink {
        val request = requestBuilder(token, "unrestrict/link")
            .post(
                FormBody.Builder()
                    .add("link", link)
                    .build()
            )
            .build()
        return json.decodeFromString(
            RealDebridUnrestrictedLink.serializer(),
            execute(request),
        )
    }

    suspend fun addMagnet(
        token: String,
        magnet: String,
    ): RealDebridAddedTorrent {
        val request = requestBuilder(token, "torrents/addMagnet")
            .post(
                FormBody.Builder()
                    .add("magnet", magnet)
                    .build()
            )
            .build()
        return json.decodeFromString(
            RealDebridAddedTorrent.serializer(),
            execute(request),
        )
    }

    suspend fun selectAllTorrentFiles(
        token: String,
        torrentId: String,
    ) {
        val request = requestBuilder(token, "torrents/selectFiles/$torrentId")
            .post(
                FormBody.Builder()
                    .add("files", "all")
                    .build()
            )
            .build()
        execute(request)
    }

    suspend fun getTorrentInfo(
        token: String,
        torrentId: String,
    ): RealDebridTorrentInfo {
        val request = requestBuilder(token, "torrents/info/$torrentId")
            .get()
            .build()
        return json.decodeFromString(
            RealDebridTorrentInfo.serializer(),
            execute(request),
        )
    }

    companion object {
        private const val BASE_URL = "https://api.real-debrid.com/rest/1.0/"
    }
}
