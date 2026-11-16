package com.abdownloadmanager.shared.util.debrid

import ir.amirab.util.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

data class DebridLink(
    val link: String,
    val fileName: String?,
)

class AllDebrid(
    private val apiKey: String,
    private val client: OkHttpClient,
) {
    suspend fun isSupported(link: String): Boolean {
        val host = link.toHttpUrlOrNull()?.host ?: return false
        return supportedDomains().any { host == it || host.endsWith(".$it") }
    }

    suspend fun unlock(link: String): DebridLink {
        val data = get<UnlockData>("link/unlock", "link" to link)
        val unlocked = data.link
        if (unlocked.isNullOrBlank()) {
            error("AllDebrid: unable to unlock this link")
        }
        return DebridLink(unlocked, data.filename?.takeIf { it.isNotBlank() })
    }

    private suspend fun supportedDomains(): List<String> = domainsLock.withLock {
        cachedDomains ?: get<DomainsData>("hosts/domains").hosts.also { cachedDomains = it }
    }

    private suspend inline fun <reified T> get(path: String, vararg params: Pair<String, String>): T {
        val url = "$BASE_URL/$path".toHttpUrl()
            .newBuilder()
            .addQueryParameter("agent", AGENT)
            .apply { params.forEach { (name, value) -> addQueryParameter(name, value) } }
            .build()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .build()
        val body = client.newCall(request).await().use { response ->
            requireNotNull(response.body) { "response.body is null" }.string()
        }
        val result = json.decodeFromString<Response<T>>(body)
        if (result.status != "success" || result.data == null) {
            error(result.error?.message ?: "AllDebrid: request failed")
        }
        return result.data
    }

    @Serializable
    private data class Response<T>(
        val status: String,
        val data: T? = null,
        val error: Error? = null,
    )

    @Serializable
    private data class Error(
        val code: String? = null,
        val message: String? = null,
    )

    @Serializable
    private data class UnlockData(
        val link: String? = null,
        val filename: String? = null,
    )

    @Serializable
    private data class DomainsData(
        val hosts: List<String> = emptyList(),
    )

    private companion object {
        const val BASE_URL = "https://api.alldebrid.com/v4"
        const val AGENT = "ABDownloadManager"
        val json = Json { ignoreUnknownKeys = true }
        val domainsLock = Mutex()
        var cachedDomains: List<String>? = null
    }
}
