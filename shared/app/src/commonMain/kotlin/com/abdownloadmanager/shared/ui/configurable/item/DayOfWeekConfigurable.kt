package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DayOfWeek

class DayOfWeekConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Set<DayOfWeek>>,
    describe: (Set<DayOfWeek>) -> StringSource,
    validate: (Set<DayOfWeek>) -> Boolean,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<Set<DayOfWeek>>(
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
