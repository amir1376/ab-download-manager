package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.android.ui.configurable.SheetInput
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.StringConfigurable
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon

object StringConfigurableRenderer : ConfigurableRenderer<StringConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: StringConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderStringConfig(configurable, configurableUiProps)
    }

    @Composable
    fun RenderStringConfig(cfg: StringConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
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
                NextIcon()
            }
        )
        val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
        SheetInput(
            configurable = cfg,
            isOpened = isOpened,
            onDismiss = onDismiss,
            inputContent = { params ->
                MyTextField(
                    modifier = params.modifier.fillMaxWidth(),
                    text = params.editingValue,
                    onTextChange = {
                        params.setEditingValue(it)
                    },
                    shape = myShapes.defaultRounded,
                    textPadding = PaddingValues(8.dp),
                    placeholder = "",
                    interactionSource = interactionSource,
                    keyboardActions = params.keyboardActions,
                )
            },
            onConfirm = {
                cfg.set(it)
                onDismiss()
            },
        )

    }
}
