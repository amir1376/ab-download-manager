package com.abdownloadmanager.shared.utils.proxy

import ir.amirab.downloader.connection.proxy.Proxy
import ir.amirab.downloader.connection.proxy.ProxyStrategy
import ir.amirab.downloader.connection.proxy.ProxyStrategyProvider
import ir.amirab.downloader.connection.proxy.ProxyType
import ir.amirab.util.UrlUtils
import ir.amirab.util.wildcardMatch
import java.net.Authenticator
import java.net.PasswordAuthentication

class ProxyManager(
    val storage: IProxyStorage,
) : ProxyStrategyProvider {
    val proxyData = storage.proxyDataFlow

    init {
        val mySocksProxyAuthenticator = MySocksProxyAuthenticator { proxyData.value.proxyWithRules.proxy }
        Authenticator.setDefault(mySocksProxyAuthenticator)
    }

    /**
     * I don't like this it's better to improve this later
     */
    private fun getProxyModeForThisURL(url: String): ProxyStrategy {
        val usingProxy = proxyData.value
        return when (usingProxy.proxyMode) {
            ProxyMode.Direct -> ProxyStrategy.Direct
            ProxyMode.UseSystem -> ProxyStrategy.UseSystem
            ProxyMode.Manual -> {
                val proxyWithRules = usingProxy.proxyWithRules
                if (shouldUseProxyFor(url, proxyWithRules.rules)) {
                    ProxyStrategy.ManualProxy(proxyWithRules.proxy)
                } else {
                    ProxyStrategy.Direct
                }
            }
            ProxyMode.Pac -> {
                val pacURI = usingProxy.pac.uri
                if (UrlUtils.isValidUrl(pacURI)) {
                    ProxyStrategy.ByScript(pacURI)
                } else {
                    ProxyStrategy.Direct
                }
            }
        }
    }

    private fun shouldUseProxyFor(
        url: String,
        rules: ProxyRules,
    ): Boolean {
        val isInExcludeList = rules.excludeURLPatterns.any {
            wildcardMatch(it, url)
        }
        return !isInExcludeList
    }

    override fun getProxyStrategyFor(url: String): ProxyStrategy {
        return getProxyModeForThisURL(url)
    }
}

/**
 * this is used for socks proxy authentication
 */
private class MySocksProxyAuthenticator(
    val currentProxy: () -> Proxy,
) : Authenticator() {
    override fun getPasswordAuthentication(): PasswordAuthentication? {
        val proxy = currentProxy()
        if (proxy.type == ProxyType.SOCKS && requestingPrompt == "SOCKS authentication") {
            if (proxy.host == requestingHost && proxy.port == requestingPort) {
                if (proxy.username != null) {
                    return PasswordAuthentication(
                        proxy.username,
                        proxy.password.orEmpty().toCharArray(),
                    )
                }
            }
        }
        return null
    }
}
