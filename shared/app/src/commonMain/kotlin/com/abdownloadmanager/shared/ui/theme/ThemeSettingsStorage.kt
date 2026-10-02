package com.abdownloadmanager.shared.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow

interface ThemeSettingsStorage {
    val themePack: MutableStateFlow<String>
    val darkMode: MutableStateFlow<String>
}
