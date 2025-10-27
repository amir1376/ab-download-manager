package com.abdownloadmanager.shared.storage

import androidx.datastore.core.DataStore
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import com.abdownloadmanager.shared.util.perhostsettings.IPerHostSettingsStorage
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsItem
import kotlinx.coroutines.flow.MutableStateFlow

class PerHostSettingsDatastoreStorage(
    dataStore: DataStore<List<PerHostSettingsItem>>,
) : IPerHostSettingsStorage, ConfigBaseSettingsByJson<List<PerHostSettingsItem>>(dataStore) {
    override val perHostSettingsFlow: MutableStateFlow<List<PerHostSettingsItem>> = data
}
