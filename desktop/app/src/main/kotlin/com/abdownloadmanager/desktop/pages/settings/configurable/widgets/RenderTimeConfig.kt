package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.TimeConfigurable
import com.abdownloadmanager.shared.ui.widget.IntTextField
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime

@Composable
fun RenderTimeConfig(cfg: TimeConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val enabled= isConfigEnabled()
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
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, true)
        },
        value = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IntTextField(
                    value=hour,
                    onValueChange = {
                        hour=it
                    },
                    range = 0..23,
                    modifier = textFieldModifier,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = "hour",
                    prettify = {it.toString().padStart(2,'0')},
                )
                Text(":",Modifier.padding(horizontal = 4.dp))
                IntTextField(
                    value=minute,
                    onValueChange = {
                        minute=it
                    },
                    range = 0..59,
                    modifier = textFieldModifier,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = "minute",
                    prettify = {it.toString().padStart(2,'0')},
                )
            }
        }
    )
}
