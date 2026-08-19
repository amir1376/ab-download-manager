package com.abdownloadmanager.shared.storage.impl

import androidx.datastore.core.DataStore
import com.abdownloadmanager.shared.storage.DnsSettings
import com.abdownloadmanager.shared.storage.IDNSSettingsStorage
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import com.abdownloadmanager.shared.util.dns.DNSOption
import com.abdownloadmanager.shared.util.dns.DnsOptionProvider
import com.abdownloadmanager.shared.util.dns.toDnsOptionOrDefault
import ir.amirab.util.singleEntryCache
import kotlinx.coroutines.flow.MutableStateFlow

class DNSStorage(
    dataStore: DataStore<DnsSettings>,
) : IDNSSettingsStorage,
    ConfigBaseSettingsByJson<DnsSettings>(dataStore),
    DnsOptionProvider {
    override val dnsSettingsFlow: MutableStateFlow<DnsSettings> = data

    val lastCachedValue = singleEntryCache<DnsSettings, DNSOption>()
    override fun getDNSOption(): DNSOption {
        return lastCachedValue.getOrCreate(dnsSettingsFlow.value) {
            it.toDnsOptionOrDefault()
        }
    }
}
