package ir.amirab.downloader.connection.proxy

import kotlinx.serialization.Serializable

@Serializable
data class Proxy(
    val type: ProxyType,
    val host: String,
    val port: Int,
    val username: String?,
    val password: String?,
) {
    companion object {
        fun default() = Proxy(
            type = ProxyType.HTTP,
            host = "127.0.0.1",
            port = 2080,
            username = null,
            password = null,
        )
    }
}