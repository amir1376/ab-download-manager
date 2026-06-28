package com.xeton.downloader.connection.proxy

interface ProxyStrategyProvider {
    fun getProxyStrategyFor(url: String): ProxyStrategy
}
