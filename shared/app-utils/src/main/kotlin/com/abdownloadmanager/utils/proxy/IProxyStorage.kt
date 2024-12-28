package com.abdownloadmanager.utils.proxy

import kotlinx.coroutines.flow.MutableStateFlow

interface IProxyStorage {
    val proxyDataFlow: MutableStateFlow<ProxyData>
}
