package com.abdownloadmanager.desktop.storage

import androidx.datastore.core.DataStore
import com.abdownloadmanager.shared.utils.ConfigBaseSettingsByJson
import com.abdownloadmanager.shared.utils.proxy.IProxyStorage
import com.abdownloadmanager.shared.utils.proxy.ProxyData

class ProxyDatastoreStorage(
    dataStore: DataStore<ProxyData>,
) : IProxyStorage, ConfigBaseSettingsByJson<ProxyData>(dataStore) {
    override val proxyDataFlow = data
}