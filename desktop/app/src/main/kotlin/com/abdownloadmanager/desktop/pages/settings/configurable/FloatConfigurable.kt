package com.abdownloadmanager.desktop.pages.settings.configurable

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
import com.abdownloadmanager.desktop.utils.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FloatConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Float>,
    val range: ClosedFloatingPointRange<Float>,
    val steps: Int = 0,
    val renderMode: RenderMode = RenderMode.TextField,

    describe: ((Float) -> StringSource),
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<Float>(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = { it in range },
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    @Composable
    override fun render(modifier: Modifier) {
        RenderFloatConfig(this, modifier)
    }

    enum class RenderMode {
        TextField,
    }
}

@Composable
private fun RenderFloatConfig(cfg: FloatConfigurable, modifier: Modifier) {
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
                FloatConfigurable.RenderMode.TextField -> {
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
