package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BooleanConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Boolean>,
    describe: ((Boolean) -> StringSource),
    val renderMode: RenderMode = RenderMode.Switch,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<Boolean>(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = { true },
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey() = Key

    enum class RenderMode {
        Checkbox, Switch,
    }
}
