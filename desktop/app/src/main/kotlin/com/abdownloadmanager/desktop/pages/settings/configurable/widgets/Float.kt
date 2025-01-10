package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.FloatConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.FloatConfigurable.RenderMode.*
import com.abdownloadmanager.shared.ui.widget.FloatTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RenderFloatConfig(cfg: FloatConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val enabled= isConfigEnabled()
    ConfigTemplate(
        modifier=modifier,
        title = {
            TitleAndDescription(cfg,true)
        },
        value = {
            when(cfg.renderMode){
                TextField -> {
                    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

                    val modifier = Modifier.width(100.dp)
                    FloatTextField(
                        value = value,
                        onValueChange = { v ->
                            setValue(v)
                        },
                        interactionSource = interactionSource,
                        range = cfg.range,
                        modifier = modifier,
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = "",
                    )
                }
            }
        }
    )
}