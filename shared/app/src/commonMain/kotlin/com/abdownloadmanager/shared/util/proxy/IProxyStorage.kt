package com.abdownloadmanager.shared.util.proxy

import kotlinx.coroutines.flow.MutableStateFlow

interface IProxyStorage {
    val proxyDataFlow: MutableStateFlow<ProxyData>
}
