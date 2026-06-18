package com.abdownloadmanager.desktop.utils.proxy

import com.github.markusbernhardt.proxy.selector.misc.BufferedProxySelector

class ProxyCachingConfig(
    val pacCacheSize: Int,
    val pacCacheTTL: Long,
    val pacCacheScope: BufferedProxySelector.CacheScope
) {
    companion object {
        fun default() = ProxyCachingConfig(
            pacCacheSize = 10,
            pacCacheTTL = 60 * 60 * 1000,
            pacCacheScope = BufferedProxySelector.CacheScope.CACHE_SCOPE_URL
        )
    }
}
