package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.configurable.item.NetworkInterfacesConfigurable
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.ifThen

object NetworkInterfacesConfigurableRenderer : ConfigurableRenderer<NetworkInterfacesConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: NetworkInterfacesConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderNetworkInterfacesConfigurable(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderNetworkInterfacesConfigurable(
        cfg: NetworkInterfacesConfigurable,
        configurableUiProps: ConfigurableUiProps,
    ) {
        val selected by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
        val enabled = isConfigEnabled()
        val labelOf: (String) -> String = { id ->
            cfg.availableOptions.firstOrNull { it.first == id }?.second ?: id
        }

        fun select(id: String) {
            if (!enabled) return
            // toggle: clicking the already-selected one clears the binding
            setValue(if (selected == id) null else id)
        }

        ConfigTemplate(
            modifier = configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, true)
            },
            value = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .ifThen(!enabled) { alpha(0.5f) }
                ) {
                    if (cfg.availableOptions.isEmpty()) {
                        Text(
                            text = Res.string.queue_network_interface_none_found
                                .asStringSource().rememberString(),
                            fontSize = myTextSizes.sm,
                            color = myColors.onBackground / 70,
                        )
                    } else {
                        FlowRow(
                            Modifier.fillMaxWidth()
                        ) {
                            cfg.availableOptions.forEach { (id, label) ->
                                val isSelected = id == selected
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .ifThen(isSelected) {
                                            background(myColors.primary / 20)
                                        }
                                        .clickable(enabled = enabled) { select(id) }
                                        .padding(vertical = 4.dp)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    MyIcon(
                                        MyIcons.check,
                                        null,
                                        Modifier.size(12.dp)
                                            .alpha(if (isSelected) 1f else 0f),
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        text = label,
                                        modifier = Modifier.alpha(if (isSelected) 1f else 0.6f),
                                        softWrap = false,
                                        fontSize = myTextSizes.base,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    val currentSelection = selected
                    Text(
                        text = if (currentSelection == null) {
                            Res.string.queue_network_interface_default.asStringSource().rememberString()
                        } else {
                            labelOf(currentSelection)
                        },
                        fontSize = myTextSizes.sm,
                        color = myColors.onBackground / 60,
                    )
                }
            }
        )
    }
}
