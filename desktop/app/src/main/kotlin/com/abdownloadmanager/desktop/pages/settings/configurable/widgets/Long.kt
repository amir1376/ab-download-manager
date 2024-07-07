package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.LongConfigurable
import com.abdownloadmanager.desktop.ui.widget.LongTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


private operator fun LongRange.get(index: Int): Long {
    return (start + index).also {
        if (it > last) {
            throw IndexOutOfBoundsException("$it bigger that $last")
        }
    }
}

@Composable
fun RenderLongConfig(cfg: LongConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val enabled = isConfigEnabled()
    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, true)
        },
        value = {
            when (cfg.renderMode) {
                LongConfigurable.RenderMode.TextField -> {
                    val interactionSource = remember { MutableInteractionSource() }
                    LongTextField(
                        value = value,
                        onValueChange = { v ->
                            setValue(v)
                        },
//                        colors = TextFieldDefaults.textFieldColors(
//                            backgroundColor = Color.Transparent
//                        ),
                        modifier = Modifier.width(200.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        interactionSource = interactionSource,
                        range = cfg.range,
                        enabled = enabled,
                    )
                }
            }
        })
}