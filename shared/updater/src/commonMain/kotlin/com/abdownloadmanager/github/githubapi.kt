package com.abdownloadmanager.github

import ir.amirab.util.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class Asset(
    @SerialName("name")
    val name: String,
    @SerialName("browser_download_url")
    val downloadLink: String,
)

@Serializable
data class Release(
    @SerialName("tag_name")
    val version: String,
    @SerialName("body")
    val body: String? = null,
    @SerialName("assets")
    val assets: List<Asset>,
)

class GithubApi(
    private val owner: String,
    private val repo: String,
    private val client: OkHttpClient,
) {
    val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getLatestReleases(): Release {
        val response = client.newCall(
            Request.Builder()
                .url("https://api.github.com/repos/${owner}/${repo}/releases/latest")
                .build()
        ).await()
        response.use {
            if (!response.isSuccessful) {
                error(response.message)
            }
            val release = json.decodeFromString<Release>(
                response.body!!.string()
            )
            return release
        }
    }
}
