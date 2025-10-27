package ir.amirab.downloader.connection.proxy

interface ProxyStrategyProvider {
    fun getProxyStrategyFor(url: String): ProxyStrategy
}
