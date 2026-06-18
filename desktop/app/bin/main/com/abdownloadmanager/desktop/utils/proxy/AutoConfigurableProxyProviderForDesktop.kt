package com.abdownloadmanager.desktop.utils.proxy

import com.github.markusbernhardt.proxy.selector.misc.BufferedProxySelector
import com.github.markusbernhardt.proxy.selector.misc.ProxyListFallbackSelector
import com.github.markusbernhardt.proxy.selector.pac.PacProxySelector
import com.github.markusbernhardt.proxy.selector.pac.UrlPacScriptSource
import ir.amirab.downloader.connection.proxy.AutoConfigurableProxyProvider
import java.net.ProxySelector

class AutoConfigurableProxyProviderForDesktop(
    private val proxyCachingConfig: ProxyCachingConfig
) : AutoConfigurableProxyProvider {
    @Volatile
    private var packProxySelector: ProxySelector? = null

    @Volatile
    private var lastUsedUri: String? = null
    override fun getAutoConfigurableProxy(uri: String): ProxySelector? {
        if (lastUsedUri == uri) {
            val o = packProxySelector
            return o ?: createAndInitializePacProxySelector(uri)
        } else {
            return createAndInitializePacProxySelector(uri)
        }
    }

    private fun createAndInitializePacProxySelector(uri: String): ProxySelector {
        synchronized(this) {
            val s = installBufferingAndFallbackBehaviour(PacProxySelector(UrlPacScriptSource(uri)))
            lastUsedUri = uri
            packProxySelector = s
            return s
        }
    }

    private fun installBufferingAndFallbackBehaviour(selector: ProxySelector): ProxySelector {
        var selector = selector
        if (selector is PacProxySelector) {
            if (proxyCachingConfig.pacCacheSize > 0) {
                selector = BufferedProxySelector(
                    proxyCachingConfig.pacCacheSize,
                    proxyCachingConfig.pacCacheTTL,
                    selector,
                    proxyCachingConfig.pacCacheScope
                )
            }
            selector = ProxyListFallbackSelector(selector)
        }
        return selector
    }
}
