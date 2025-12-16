package ir.amirab.downloader.connection.proxy

import java.net.ProxySelector

interface SystemProxySelectorProvider {
    fun getSystemProxySelector(): ProxySelector?
}

class NoopSystemProxySelectorProvider : SystemProxySelectorProvider {
    override fun getSystemProxySelector(): ProxySelector? {
        println("System proxy not available")
        return null
    }
}
