package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.configurable.item.TimeConfigurable
import com.abdownloadmanager.shared.ui.widget.IntTextField
import com.abdownloadmanager.shared.ui.widget.Text
import kotlinx.datetime.LocalTime

object TimeConfigurableRenderer : ConfigurableRenderer<TimeConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: TimeConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderTimeConfig(configurable, configurableUiProps)
    }

    @Composable
    fun RenderTimeConfig(cfg: TimeConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
        val enabled = isConfigEnabled()
        var hour by remember(value) {
            mutableStateOf(value.hour)
        }
        var minute by remember(value) {
            mutableStateOf(value.minute)
        }
        LaunchedEffect(hour, minute) {
            setValue(
                LocalTime(
                    hour = hour, minute = minute,
                )
            )
        }

        val textFieldModifier = Modifier.width(64.dp)
        ConfigTemplate(
            modifier = configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, true)
            },
            value = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IntTextField(
                        value = hour,
                        onValueChange = {
                            hour = it
                        },
                        range = 0..23,
                        modifier = textFieldModifier,
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = "hour",
                        prettify = { it.toString().padStart(2, '0') },
                    )
                    Text(":", Modifier.padding(horizontal = 4.dp))
                    IntTextField(
                        value = minute,
                        onValueChange = {
                            minute = it
                        },
                        range = 0..59,
                        modifier = textFieldModifier,
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = "minute",
                        prettify = { it.toString().padStart(2, '0') },
                    )
                }
            }
        )
    }
}
