package ir.amirab.downloader.connection.proxy

import java.net.ProxySelector
import java.net.URI

interface AutoConfigurableProxyProvider {
    fun getAutoConfigurableProxy(
        uri: String
    ): ProxySelector?

    class NoOp : AutoConfigurableProxyProvider {
        override fun getAutoConfigurableProxy(uri: String): ProxySelector? {
            return null
        }
    }
}
