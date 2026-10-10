package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.RenderSpinnerInSheet
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.configurable.item.EnumConfigurable
import com.abdownloadmanager.shared.ui.widget.Text

object EnumConfigurableRenderer : ConfigurableRenderer<EnumConfigurable<Any>> {
    @Composable
    override fun RenderConfigurable(configurable: EnumConfigurable<Any>, configurableUiProps: ConfigurableUiProps) {
        RenderEnumConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun <T> RenderEnumConfig(cfg: EnumConfigurable<T>, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
        val index = remember(cfg.possibleValues, value) {
            cfg.possibleValues.indexOf(value)
        }
        val enabled = isConfigEnabled()
        val describeShort = remember(value) {
            cfg.itemTitle(value)
        }.rememberString()

        val shouldShowDescribe = cfg.itemTitle != cfg.describe

        var isOpened by remember { mutableStateOf(false) }
        val onDismiss = {
            isOpened = false
        }
        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable {
                    isOpened = true
                }
                .padding(configurableUiProps.itemPaddingValues),
            title = {
                Column {
                    val describeLong = remember(cfg, value, shouldShowDescribe) {
                        if (shouldShowDescribe) {
                            cfg.describe(value)
                        } else null
                    }?.rememberString()
                    TitleAndDescription(
                        cfg = cfg,
                        describeContent = describeShort,
                        describeSecondContent = describeLong,
                    )
                }
            },
            value = {
                NextIcon()
            }
        )
        RenderSpinnerInSheet(
            title = cfg.title,
            onDismiss = onDismiss,
            isOpened = isOpened,
            possibleValues = cfg.possibleValues,
            value = value,
            onSelect = {
                setValue(it)
                onDismiss()
            },
            valueToString = cfg.valueToString,
            render = {
                val title = remember(cfg, it) {
                    cfg.itemTitle(it)
                }
                Text(title.rememberString())
            })
    }
}
