package com.abdownloadmanager.desktop.ui.configurable.platform.item

import com.abdownloadmanager.desktop.pages.settings.FontInfo
import com.abdownloadmanager.shared.ui.configurable.BaseEnumConfigurable
import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FontConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<FontInfo>,
    describe: (FontInfo) -> StringSource,
    possibleValues: List<FontInfo>,
    valueToString: (FontInfo) -> List<String> = {
        listOf(it.name.getString())
    },
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : BaseEnumConfigurable<FontInfo>(
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
