package com.abdownloadmanager.desktop.utils.configurable

import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseEnumConfigurable<T>(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<T>,
    describe: ((T) -> StringSource),
    val possibleValues: List<T>,
    val valueToString: (T) -> List<String>,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<T>(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = {
        it in possibleValues
    },
    describe = describe,
    enabled = enabled,
    visible = visible,
)
