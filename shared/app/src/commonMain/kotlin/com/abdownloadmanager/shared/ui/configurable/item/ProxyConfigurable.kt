package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.util.proxy.ProxyData
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProxyConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<ProxyData>,
    describe: (ProxyData) -> StringSource,
    validate: (ProxyData) -> Boolean,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<ProxyData>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    validate = validate,
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey() = Key
}
