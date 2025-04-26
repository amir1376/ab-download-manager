package com.abdownloadmanager.desktop.pages.settings.configurable

import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.abdownloadmanager.desktop.utils.configurable.Configurable
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
    @Composable
    override fun render(modifier: Modifier) {
        RenderBooleanConfig(this, modifier)
    }

    enum class RenderMode {
        Checkbox, Switch,
    }
}


@Composable
private fun RenderBooleanConfig(
    cfg: BooleanConfigurable,
    modifier: Modifier,
) {
    val checked = cfg.stateFlow.collectAsState().value
    val setValue = cfg::set
    val enabled = isConfigEnabled()
    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, true)
        },
        value = {
            when (cfg.renderMode) {
                BooleanConfigurable.RenderMode.Checkbox -> {
                    CheckBox(
                        value = checked,
                        enabled = enabled,
                        onValueChange = {
                            setValue(it)
                        }
                    )
                }

                BooleanConfigurable.RenderMode.Switch -> {
                    Switch(
                        checked = checked,
                        enabled = enabled,
                        onCheckedChange = {
                            setValue(it)
                        }
                    )
                }
            }
        })
}
