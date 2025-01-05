package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable.RenderMode.*
import com.abdownloadmanager.shared.ui.widget.IntTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


private operator fun IntRange.get(index: Int): Int {
    return (start + index).also {
        if (it > last) {
            throw IndexOutOfBoundsException("$it bigger that $last")
        }
    }

}

@Composable
fun RenderIntegerConfig(cfg: IntConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val enabled= isConfigEnabled()
    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, true)
        },
        value = {
            when (cfg.renderMode) {
                TextField -> {
                    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
                    IntTextField(
                        value = value,
                        onValueChange = { v ->
                            setValue(v)
                        },
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        backgroundColor = Color.Transparent
//                    ),
                        interactionSource = interactionSource,
                        range = cfg.range,
                        modifier = Modifier.width(100.dp),
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = "",
                    )
                }
            }
        })
}