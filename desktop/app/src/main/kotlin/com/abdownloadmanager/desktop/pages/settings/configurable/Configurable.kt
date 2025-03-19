package com.abdownloadmanager.desktop.pages.settings.configurable

import com.abdownloadmanager.desktop.pages.settings.ThemeInfo
import com.abdownloadmanager.shared.utils.FileChecksum
import com.abdownloadmanager.shared.utils.FileChecksumAlgorithm
import com.abdownloadmanager.shared.utils.proxy.ProxyData
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

private val DefaultEnabledValue get() = MutableStateFlow(true)
private val DefaultVisibleValue get() = MutableStateFlow(true)

sealed class Configurable<T>(
    val title: StringSource,
    val description: StringSource,
    val backedBy: MutableStateFlow<T>,
    val validate: (T) -> Boolean = { true },
    val describe: (T) -> StringSource,
    val enabled: StateFlow<Boolean> = DefaultEnabledValue,
    val visible: StateFlow<Boolean> = DefaultVisibleValue,
) {
    val stateFlow = backedBy.asStateFlow()
    fun set(value: T): Boolean {
        if (validate(value)) {
            // don't use update function here maybe this is a mappedByTwoWayMutableStateFlow
            // IMPROVE
            backedBy.value = value
            return true
        }
        return false
    }
}

//primitives
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
    enum class RenderMode {
        TextField,
    }
}

sealed class BaseLongConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Long>,
    describe: ((Long) -> StringSource),
    val range: LongRange,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<Long>(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = {
        it in range
    },
    describe = describe,
    enabled = enabled,
    visible = visible,
)

class LongConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Long>,
    describe: ((Long) -> StringSource),
    range: LongRange,
    val renderMode: RenderMode = RenderMode.TextField,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : BaseLongConfigurable(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    range = range,
    enabled = enabled,
    visible = visible,
) {
    enum class RenderMode {
        TextField,
    }
}

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
    enum class RenderMode {
        Checkbox, Switch,
    }
}

class FloatConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Float>,
    val range: ClosedFloatingPointRange<Float>,
    val steps: Int = 0,
    val renderMode: RenderMode = RenderMode.TextField,

    describe: ((Float) -> StringSource),
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<Float>(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = { it in range },
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    enum class RenderMode {
        TextField,
    }
}

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
)

class FolderConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<String>,
    describe: ((String) -> StringSource),
    validate: (String) -> Boolean,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : StringConfigurable(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = validate,
    describe = describe,
    enabled = enabled,
    visible = visible,
)

sealed class BaseEnumConfigurable<T>(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<T>,
    describe: ((T) -> StringSource),
    val possibleValues: List<T>,
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

//more complex
open class EnumConfigurable<T>(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<T>,
    describe: ((T) -> StringSource),
    possibleValues: List<T>,
    val renderMode: RenderMode = RenderMode.Spinner,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : BaseEnumConfigurable<T>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    possibleValues = possibleValues,
    enabled = enabled,
    visible = visible,
) {
    enum class RenderMode {
        Spinner,
    }
}

class ThemeConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<ThemeInfo>,
    describe: (ThemeInfo) -> StringSource,
    possibleValues: List<ThemeInfo>,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : BaseEnumConfigurable<ThemeInfo>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    possibleValues = possibleValues,
    enabled = enabled,
    visible = visible,
)

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
)

class FileChecksumConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<FileChecksum?>,
    describe: (FileChecksum?) -> StringSource,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<FileChecksum?>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    enabled = enabled,
    visible = visible,
)

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
)

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
)

class ProxyConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<ProxyData>,
    describe: (ProxyData) -> StringSource,
    validate: (ProxyData) -> Boolean,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<ProxyData>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    validate = validate,
    enabled = enabled,
    visible = visible,
)
