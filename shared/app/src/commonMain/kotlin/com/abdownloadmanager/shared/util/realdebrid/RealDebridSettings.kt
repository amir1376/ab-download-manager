package com.abdownloadmanager.shared.util.realdebrid

import androidx.datastore.core.DataStore
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import ir.amirab.util.flow.mapTwoWayStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class RealDebridSettings(
    val enabled: Boolean = false,
    val apiToken: String = "",
) {
    val hasApiToken: Boolean get() = apiToken.isNotBlank()
    val isUsable: Boolean get() = enabled && hasApiToken
}

class RealDebridSettingsStorage(
    dataStore: DataStore<RealDebridSettings>,
) : ConfigBaseSettingsByJson<RealDebridSettings>(dataStore) {
    val enabled = data.mapTwoWayStateFlow(
        map = { it.enabled },
        unMap = { copy(enabled = it) },
    )
    val apiToken = data.mapTwoWayStateFlow(
        map = { it.apiToken },
        unMap = { copy(apiToken = it) },
    )
}
