package com.abdownloadmanager.shared.utils.proxy

import ir.amirab.downloader.connection.proxy.Proxy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProxyRules(
    val excludeURLPatterns: List<String>,
)

@Serializable
data class ProxyWithRules(
    val proxy: Proxy,
    val rules: ProxyRules,
)

enum class ProxyMode {
    @SerialName("direct")
    Direct,

    @SerialName("system")
    UseSystem,

    @SerialName("manual")
    Manual;

    companion object {
        fun usableValues(): List<ProxyMode> {
            // UseSystem not works as expected
            // so we filter it for now.
            return listOf(
                Direct,
                Manual,
            )
        }
    }
}

// for persisting in storage
@Serializable
data class ProxyData(
    val proxyMode: ProxyMode,
    val proxyWithRules: ProxyWithRules,
) {
    companion object {
        fun default() = ProxyData(
            proxyMode = ProxyMode.Direct,
            proxyWithRules = ProxyWithRules(
                proxy = Proxy.default(),
                rules = ProxyRules(emptyList())
            )
        )
    }
}