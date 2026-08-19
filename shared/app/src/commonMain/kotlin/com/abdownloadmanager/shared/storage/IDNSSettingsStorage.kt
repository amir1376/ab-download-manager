package com.abdownloadmanager.shared.storage

import com.abdownloadmanager.shared.util.dns.DnsModes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class DnsSettings(
    val mode: DnsModes = DnsModes.System,
    val address: String = "",
)

interface IDNSSettingsStorage {
    val dnsSettingsFlow: MutableStateFlow<DnsSettings>
}

