package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.storage.DnsSettings
import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DnsConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<DnsSettings>,
    describe: (DnsSettings) -> StringSource,
    validate: (DnsSettings) -> Boolean,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<DnsSettings>(
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
