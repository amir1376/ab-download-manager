package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.BaseLongConfigurable
import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeedLimitConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Long>,
    describe: (Long) -> StringSource,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : BaseLongConfigurable(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    range = 0..Long.MAX_VALUE,
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey() = Key
}
