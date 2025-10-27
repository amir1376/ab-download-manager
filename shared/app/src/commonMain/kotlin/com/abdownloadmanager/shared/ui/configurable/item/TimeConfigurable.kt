package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalTime

class TimeConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<LocalTime>,
    describe: (LocalTime) -> StringSource,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<LocalTime>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey() = Key
}
