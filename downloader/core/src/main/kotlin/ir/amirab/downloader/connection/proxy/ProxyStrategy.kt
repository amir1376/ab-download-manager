package ir.amirab.downloader.connection.proxy

sealed interface ProxyStrategy {
    data object Direct : ProxyStrategy
    data object UseSystem : ProxyStrategy
    data class ManualProxy(val proxy: Proxy) : ProxyStrategy
}