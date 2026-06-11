package com.abdownloadmanager.shared.ui.configurable.item

import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SoundFileConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<String>,
    describe: ((String) -> StringSource),
    validate: (String) -> Boolean,
    onPreview: (() -> Unit)? = null,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : FileConfigurable(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = validate,
    describe = describe,
    onPreview = onPreview,
    enabled = enabled,
    visible = visible,
)
