package com.abdownloadmanager.integration.client

import com.abdownloadmanager.integration.IdRequest
import com.abdownloadmanager.integration.IdsRequest
import com.abdownloadmanager.integration.RemoveRequest
import com.abdownloadmanager.integration.NewDownloadTask
import com.abdownloadmanager.integration.HttpDownloadCredentialsFromIntegration
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.http4k.client.OkHttp
import org.http4k.core.*

/**
 * HTTP client for CLI commands to communicate with the desktop app's
 * embedded integration server.
 *
 * Uses http4k Client(OkHttp) — the same HTTP stack as the desktop app's
 * SingleInstance communication (see Comunication.kt).
 *
 * @param port The browserIntegrationPort from appSettings.json (typically 15151).
 */
class DesktopClient(private val port: Int) {

    private val client by lazy { OkHttp() }
    private val json = Json { ignoreUnknownKeys = true }

    // --- Low-level HTTP ---

    private fun request(method: Method, path: String, body: String? = null): DesktopResult {
        val uri = Uri.of("http://127.0.0.1:$port$path")
        val request = if (body != null) {
            Request(method, uri).body(body)
        } else {
            Request(method, uri)
        }
        return try {
            val response = client(request)
            val responseBody = response.bodyString()
            if (response.status.successful) {
                DesktopResult.Success(responseBody)
            } else {
                DesktopResult.RemoteError(response.status.code, responseBody)
            }
        } catch (e: Exception) {
            DesktopResult.ConnectionError(e.message ?: "Unknown connection error")
        }
    }

    // --- Health check ---

    /** Ping the desktop app to check if it's running and responsive. */
    fun ping(): Boolean {
        val result = request(Method.POST, "/ping")
        return result is DesktopResult.Success && result.data == "pong"
    }

    // --- Business methods ---

    /** List all downloads from the desktop app. */
    fun listDownloads(): DesktopResult {
        return request(Method.GET, "/list")
    }

    /** Get detailed info for a single download by ID. */
    fun getDownloadInfo(id: Long): DesktopResult {
        val body = json.encodeToString(IdRequest.serializer(), IdRequest(id))
        return request(Method.POST, "/info", body)
    }

    /** Pause one or more downloads by ID. */
    fun pauseDownloads(ids: List<Long>): DesktopResult {
        val body = json.encodeToString(IdsRequest.serializer(), IdsRequest(ids))
        return request(Method.POST, "/pause", body)
    }

    /** Resume one or more downloads by ID. */
    fun resumeDownloads(ids: List<Long>): DesktopResult {
        val body = json.encodeToString(IdsRequest.serializer(), IdsRequest(ids))
        return request(Method.POST, "/resume", body)
    }

    /** Remove one or more downloads by ID. */
    fun removeDownloads(ids: List<Long>, keepFile: Boolean = true): DesktopResult {
        val body = json.encodeToString(RemoveRequest.serializer(), RemoveRequest(ids, keepFile))
        return request(Method.POST, "/remove", body)
    }

    /**
     * Add a new download via the headless endpoint.
     * Unlike /add, /start-headless-download does NOT open a GUI dialog.
     */
    fun addDownload(
        url: String,
        name: String? = null,
        folder: String? = null,
        username: String? = null,
        password: String? = null,
        queueId: Long? = null,
    ): DesktopResult {
        val headers = if (username != null && password != null) {
            val encoded = java.util.Base64.getEncoder().encodeToString("$username:$password".toByteArray())
            mapOf("Authorization" to "Basic $encoded")
        } else {
            null
        }
        val downloadSource = HttpDownloadCredentialsFromIntegration(
            link = url,
            headers = headers,
            suggestedName = name,
        )
        val task = NewDownloadTask(
            downloadSource = downloadSource,
            folder = folder,
            name = name,
            queueId = queueId,
        )
        val body = json.encodeToString(NewDownloadTask.serializer(), task)
        return request(Method.POST, "/start-headless-download", body)
    }
}