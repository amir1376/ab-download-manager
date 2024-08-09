package com.abdownloadmanager.desktop.pages.settings.configurable

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.abdownloadmanager.desktop.pages.settings.ThemeInfo
import com.abdownloadmanager.desktop.pages.settings.configurable.BooleanConfigurable.RenderMode
import com.abdownloadmanager.desktop.ui.theme.MyColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

private val DefaultEnabledValue get() = MutableStateFlow(true)
private val DefaultVisibleValue get() = MutableStateFlow(true)

sealed class Configurable<T>(
    val title: String,
    val description: String,
    val backedBy: MutableStateFlow<T>,
    val validate: (T) -> Boolean = { true },
    val describe: (T) -> String,
    val enabled: StateFlow<Boolean> = DefaultEnabledValue,
    val visible: StateFlow<Boolean> = DefaultVisibleValue,
) {
    val stateFlow = backedBy.asStateFlow()
    fun set(value: T): Boolean {
        if (validate(value)) {
            // don't use update function here maybe this is a mappedByTwoWayMutableStateFlow
            // IMPROVE
            backedBy.value=value
            return true
        }
        return false
    }
}

//primitives
class IntConfigurable(
    title: String,
    description: String,
    backedBy: MutableStateFlow<Int>,
    describe: ((Int) -> String),
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<Long>,
    describe: ((Long) -> String),
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<Long>,
    describe: ((Long) -> String),
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<Boolean>,
    describe: ((Boolean) -> String),
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<Float>,
    val range: ClosedFloatingPointRange<Float>,
    val steps: Int = 0,
    val renderMode: RenderMode = RenderMode.TextField,

    describe: ((Float) -> String),
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
){
    enum class RenderMode {
        TextField,
    }
}

open class StringConfigurable(
    title: String,
    description: String,
    backedBy: MutableStateFlow<String>,
    describe: ((String) -> String),
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<String>,
    describe: ((String) -> String),
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<T>,
    describe: ((T) -> String),
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<T>,
    describe: ((T) -> String),
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<ThemeInfo>,
    describe: (ThemeInfo) -> String,
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<Long>,
    describe: (Long) -> String,
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

class TimeConfigurable(
    title: String,
    description: String,
    backedBy: MutableStateFlow<LocalTime>,
    describe: (LocalTime) -> String,
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
    title: String,
    description: String,
    backedBy: MutableStateFlow<Set<DayOfWeek>>,
    describe: (Set<DayOfWeek>) -> String,
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
