package com.abdownloadmanager.shared.utils.perhostsettings

import kotlinx.coroutines.flow.MutableStateFlow

interface IPerHostSettingsStorage {
    val perHostSettingsFlow: MutableStateFlow<List<PerHostSettingsItem>>
}
