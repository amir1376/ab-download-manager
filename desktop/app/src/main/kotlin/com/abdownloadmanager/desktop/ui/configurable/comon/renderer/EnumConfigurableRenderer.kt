package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.RenderSpinner
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
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
        val shouldShowDescribe = cfg.describe != cfg.itemTitle
        val enabled = isConfigEnabled()
        ConfigTemplate(
            modifier = configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, describe = shouldShowDescribe)
            },
            value = {
                when (cfg.renderMode) {
                    EnumConfigurable.RenderMode.Spinner -> RenderSpinner(
                        possibleValues = cfg.possibleValues,
                        value = value,
                        onSelect = {
                            setValue(it)
                        },
                        valueToString = cfg.valueToString,
                        modifier = Modifier.widthIn(min = 160.dp),
                        enabled = enabled,
                        render = {
                            val itemTitle = remember(cfg, it) {
                                cfg.itemTitle(it)
                            }
                            Text(itemTitle.rememberString())
                        })
                }
            }
        )
    }

}
