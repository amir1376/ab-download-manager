package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.configurable.item.FloatConfigurable
import com.abdownloadmanager.shared.ui.widget.FloatTextField

object FloatConfigurableRenderer : ConfigurableRenderer<FloatConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: FloatConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderFloatConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderFloatConfig(cfg: FloatConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
        val enabled = isConfigEnabled()
        ConfigTemplate(
            modifier = configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, true)
            },
            value = {
                when (cfg.renderMode) {
                    FloatConfigurable.RenderMode.TextField -> {
                        val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

                        val modifier = Modifier.Companion.width(100.dp)
                        FloatTextField(
                            value = value,
                            onValueChange = { v ->
                                setValue(v)
                            },
                            interactionSource = interactionSource,
                            range = cfg.range,
                            modifier = modifier,
                            enabled = enabled,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Decimal),
                            placeholder = "",
                        )
                    }
                }
            }
        )
    }

}
