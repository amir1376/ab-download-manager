package com.abdownloadmanager.shared.storage

import androidx.datastore.core.DataStore
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import com.abdownloadmanager.shared.util.proxy.IProxyStorage
import com.abdownloadmanager.shared.util.proxy.ProxyData

class ProxyDatastoreStorage(
    dataStore: DataStore<ProxyData>,
) : IProxyStorage, ConfigBaseSettingsByJson<ProxyData>(dataStore) {
    override val proxyDataFlow = data
}
