package com.abdownloadmanager.shared.utils.proxy

import kotlinx.coroutines.flow.MutableStateFlow

interface IProxyStorage {
    val proxyDataFlow: MutableStateFlow<ProxyData>
}
