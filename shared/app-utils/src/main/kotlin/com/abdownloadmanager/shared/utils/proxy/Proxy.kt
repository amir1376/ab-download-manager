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

@Serializable
data class PACProxy(
    val uri: String,// an uri to get script path of the PAC
) {
    companion object {
        fun default() = PACProxy("http://localhost/some.pac")
    }
}

enum class ProxyMode {
    @SerialName("direct")
    Direct,

    @SerialName("system")
    UseSystem,

    @SerialName("manual")
    Manual,

    @SerialName("pac")
    Pac;

    companion object {
        fun usableValues(): List<ProxyMode> {
            // UseSystem not works as expected
            // so we filter it for now.
            return listOf(
                Direct,
                UseSystem,
                Pac,
                Manual,
            )
        }
    }
}

// for persisting in storage
@Serializable
data class ProxyData(
    val proxyMode: ProxyMode,
    //manual proxy config
    val proxyWithRules: ProxyWithRules,
    //configuration script config
    val pac: PACProxy,
) {
    companion object {
        fun default() = ProxyData(
            proxyMode = ProxyMode.Direct,
            proxyWithRules = ProxyWithRules(
                proxy = Proxy.default(),
                rules = ProxyRules(emptyList())
            ),
            pac = PACProxy.default()
        )
    }
}
