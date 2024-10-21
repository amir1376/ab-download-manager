package com.abdownloadmanager.desktop.storage

import androidx.datastore.core.DataStore
import com.abdownloadmanager.desktop.utils.ConfigBaseSettingsByJson
import com.abdownloadmanager.utils.proxy.IProxyStorage
import com.abdownloadmanager.utils.proxy.ProxyData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ProxyDatastoreStorage(
    dataStore: DataStore<ProxyData>,
) : IProxyStorage, ConfigBaseSettingsByJson<ProxyData>(dataStore) {
    override val proxyDataFlow = data
}