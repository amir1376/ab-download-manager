package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.Configurable
import androidx.compose.runtime.Stable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
data class ConfigurableGroup(
    val groupTitle: StateFlow<StringSource?> = MutableStateFlow(null),
    val mainConfigurable:Configurable<*>?=null,
    val nestedEnabled:StateFlow<Boolean> =MutableStateFlow(true),
    val nestedVisible:StateFlow<Boolean> =MutableStateFlow(true),
    val nestedConfigurable: List<Configurable<*>>,
)