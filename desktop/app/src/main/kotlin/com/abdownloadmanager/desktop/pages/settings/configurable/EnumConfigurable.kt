package com.abdownloadmanager.desktop.pages.settings.configurable

import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import com.abdownloadmanager.desktop.utils.configurable.BaseEnumConfigurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    @Composable
    override fun render(modifier: Modifier) {
        RenderEnumConfig(this, modifier)
    }

    enum class RenderMode {
        Spinner,
    }
}

@Composable
private fun <T> RenderEnumConfig(cfg: EnumConfigurable<T>, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val index = remember(cfg.possibleValues, value) {
        cfg.possibleValues.indexOf(value)
    }
    val enabled = isConfigEnabled()
    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, false)
        },
        value = {
            when (cfg.renderMode) {
                EnumConfigurable.RenderMode.Spinner -> RenderSpinner(
                    possibleValues = cfg.possibleValues,
                    value = value,
                    onSelect = {
                        setValue(it)
                    },
                    modifier = Modifier.widthIn(min = 160.dp),
                    enabled = enabled,
                    render = {
                        Text(cfg.describe(it).rememberString())
                    })
            }
        }
    )
}
