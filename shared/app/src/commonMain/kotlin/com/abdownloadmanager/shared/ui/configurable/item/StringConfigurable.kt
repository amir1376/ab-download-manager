package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class StringConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<String>,
    describe: ((String) -> StringSource),
    validate: (String) -> Boolean = { true },
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<String>(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = validate,
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey(): Configurable.Key = Key
}
