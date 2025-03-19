package ir.amirab.downloader.connection.proxy

import java.net.ProxySelector

interface SystemProxySelectorProvider {
    fun getSystemProxySelector(): ProxySelector?
}
