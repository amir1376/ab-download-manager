package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.BooleanConfigurable
import com.abdownloadmanager.desktop.ui.widget.CheckBox
import com.abdownloadmanager.desktop.ui.widget.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

@Composable
fun RenderBooleanConfig(
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
