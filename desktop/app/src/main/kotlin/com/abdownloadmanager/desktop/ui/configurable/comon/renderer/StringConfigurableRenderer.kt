package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.StringConfigurable
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.util.ui.theme.myShapes

object StringConfigurableRenderer : ConfigurableRenderer<StringConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: StringConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderStringConfig(configurable, configurableUiProps)
    }

    @Composable
    fun RenderStringConfig(cfg: StringConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
        ConfigTemplate(
            modifier = configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, true)
            },
            value = {
                MyTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = value,
                    onTextChange = {
                        setValue(it)
                    },
                    shape = myShapes.defaultRounded,
                    textPadding = PaddingValues(4.dp),
                    placeholder = "",
                )
            }
        )
    }
}
