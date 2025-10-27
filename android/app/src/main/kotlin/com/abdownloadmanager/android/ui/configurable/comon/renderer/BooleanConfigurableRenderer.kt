package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.configurable.item.BooleanConfigurable
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.Switch

object BooleanConfigurableRenderer : ConfigurableRenderer<BooleanConfigurable> {
    @Composable
    override fun RenderConfigurable(
        configurable: BooleanConfigurable,
        configurableUiProps: ConfigurableUiProps
    ) {
        RenderBooleanConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderBooleanConfig(
        cfg: BooleanConfigurable,
        configurableUiProps: ConfigurableUiProps,
    ) {
        val checked = cfg.stateFlow.collectAsState().value
        val setValue = cfg::set
        val enabled = isConfigEnabled()
        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable {
                    setValue(!checked)
                }
                .padding(configurableUiProps.itemPaddingValues),
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
}
