package com.abdownloadmanager.desktop.pages.settings.configurable

import com.abdownloadmanager.shared.ui.widget.IntTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.utils.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IntConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Int>,
    describe: ((Int) -> StringSource),
    val range: IntRange,
    val renderMode: RenderMode = RenderMode.TextField,
    enabled: StateFlow<Boolean> = Configurable.DefaultEnabledValue,
    visible: StateFlow<Boolean> = Configurable.DefaultVisibleValue,
) : Configurable<Int>(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = {
        it in range
    },
    describe = describe,
) {
    enum class RenderMode {
        TextField,
    }

    @Composable
    override fun render(modifier: Modifier) {
        RenderIntegerConfig(this, modifier)
    }
}


private operator fun IntRange.get(index: Int): Int {
    return (start + index).also {
        if (it > last) {
            throw IndexOutOfBoundsException("$it bigger that $last")
        }
    }

}

@Composable
private fun RenderIntegerConfig(cfg: IntConfigurable, modifier: Modifier) {
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
                IntConfigurable.RenderMode.TextField -> {
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


