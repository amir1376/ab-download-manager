package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IntConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Int>,
    describe: ((Int) -> StringSource),
    val range: IntRange,
    val renderMode: RenderMode = RenderMode.TextField,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<Int>(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = {
        it in range
    },
    describe = describe,
) {
    object Key : Configurable.Key

    override fun getKey() = Key

    enum class RenderMode {
        TextField,
    }
}

