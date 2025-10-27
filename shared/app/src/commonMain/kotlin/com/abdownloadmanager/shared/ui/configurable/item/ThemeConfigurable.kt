package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.BaseEnumConfigurable
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.ui.theme.ThemeInfo
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<ThemeInfo>,
    describe: (ThemeInfo) -> StringSource,
    possibleValues: List<ThemeInfo>,
    valueToString: (ThemeInfo) -> List<String> = {
        listOf(it.name.getString())
    },
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : BaseEnumConfigurable<ThemeInfo>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    possibleValues = possibleValues,
    valueToString = valueToString,
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey() = Key
}

