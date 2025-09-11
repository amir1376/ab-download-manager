package com.abdownloadmanager.shared.utils.perhostsettings

import ir.amirab.util.wildcardMatch
import kotlinx.coroutines.flow.update
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class PerHostSettingsManager(
    private val storage: IPerHostSettingsStorage
) {

    fun getStorageData(): List<PerHostSettingsItem> {
        return storage.perHostSettingsFlow.value
    }

    fun getSettingsForHost(host: String): PerHostSettingsItem? {
        return getStorageData()
            // hosts that doesn't have wildcards should be checked first
            .sortedBy {
                it.host.count { char -> char == '*' }
            }
            .firstOrNull {
                wildcardMatch(it.host, host)
            }
    }

    fun setSettingsData(hostSettingsList: List<PerHostSettingsItem>) {
        storage.perHostSettingsFlow.update {
            hostSettingsList
                .filter { it.host.isNotBlank() }
                .distinctBy { it.host }
        }
    }
}

fun PerHostSettingsManager.getSettingsForURL(url: String): PerHostSettingsItem? {
    return url
        .toHttpUrlOrNull()
        ?.host
        ?.let(::getSettingsForHost)
}
