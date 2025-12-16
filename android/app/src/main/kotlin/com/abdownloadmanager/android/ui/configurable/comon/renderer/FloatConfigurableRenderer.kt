package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.SheetInput
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.FloatConfigurable
import com.abdownloadmanager.shared.ui.widget.FloatTextField
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.widget.MyIcon

object FloatConfigurableRenderer : ConfigurableRenderer<FloatConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: FloatConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderFloatConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderFloatConfig(cfg: FloatConfigurable, configurableUiProps: ConfigurableUiProps) {
//        val value by cfg.stateFlow.collectAsState()
//        val setValue = cfg::set
//        val enabled = isConfigEnabled()

        var isOpened by remember { mutableStateOf(false) }
        val onDismiss = {
            isOpened = false
        }
        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable { isOpened = true }
                .padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, true)
            },
            value = {
                when (cfg.renderMode) {
                    FloatConfigurable.RenderMode.TextField -> {
                        NextIcon()
                        RenderTextFieldFloatInput(cfg = cfg, isOpened = isOpened, onDismiss = onDismiss)
                    }
                }
            }
        )
    }

    @Composable
    fun RenderTextFieldFloatInput(
        cfg: FloatConfigurable,
        isOpened: Boolean,
        onDismiss: () -> Unit,
    ) {
        val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

        SheetInput(
            configurable = cfg,
            isOpened = isOpened,
            onDismiss = onDismiss,
            inputContent = { params ->
                FloatTextField(
                    value = params.editingValue,
                    onValueChange = { v ->
                        params.setEditingValue(v)
                    },
                    interactionSource = interactionSource,
                    range = cfg.range,
                    modifier = params.modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    textPadding = PaddingValues(8.dp),
                    keyboardActions = params.keyboardActions,
                    placeholder = "",
                )
            },
            onConfirm = {
                cfg.set(it)
                onDismiss()
            },
        )

    }

}
