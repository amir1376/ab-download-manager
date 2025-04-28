package com.abdownloadmanager.desktop.pages.settings.configurable

import com.abdownloadmanager.shared.ui.widget.LongTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.utils.configurable.BaseLongConfigurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LongConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Long>,
    describe: ((Long) -> StringSource),
    range: LongRange,
    val renderMode: RenderMode = RenderMode.TextField,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : BaseLongConfigurable(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    range = range,
    enabled = enabled,
    visible = visible,
) {
    @Composable
    override fun render(modifier: Modifier) {
        RenderLongConfig(this, modifier)
    }

    enum class RenderMode {
        TextField,
    }
}


private operator fun LongRange.get(index: Int): Long {
    return (start + index).also {
        if (it > last) {
            throw IndexOutOfBoundsException("$it bigger that $last")
        }
    }
}

@Composable
private fun RenderLongConfig(cfg: LongConfigurable, modifier: Modifier) {
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
