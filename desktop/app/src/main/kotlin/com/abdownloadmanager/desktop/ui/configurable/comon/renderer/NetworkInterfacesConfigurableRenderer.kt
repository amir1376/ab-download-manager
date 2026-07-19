package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
        val optionsById = cfg.availableOptions.associate { (id, label) -> id to label }

        fun toggle(id: String) {
            if (!enabled) return
            setValue(
                if (id in selected) {
                    selected.minus(id)
                } else {
                    selected.plus(id)
                }
            )
        }

        fun move(index: Int, delta: Int) {
            if (!enabled) return
            val target = index + delta
            if (target < 0 || target >= selected.size) return
            val list = selected.toMutableList()
            val tmp = list[index]
            list[index] = list[target]
            list[target] = tmp
            setValue(list)
        }

        fun remove(index: Int) {
            if (!enabled) return
            setValue(selected.toMutableList().also { it.removeAt(index) })
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
                    // available interfaces: discover & select
                    if (cfg.availableOptions.isEmpty()) {
                        Text(
                            text = Res.string.queue_network_interface_none_found
                                .asStringSource().rememberString(),
                            fontSize = myTextSizes.sm,
                            color = myColors.onBackground / 70,
                        )
                    } else {
                        FlowRowItem(optionsById, selected, enabled, ::toggle)
                    }

                    Spacer(Modifier.height(8.dp))

                    // ordered selection summary
                    if (selected.isEmpty()) {
                        Text(
                            text = Res.string.queue_network_interface_default
                                .asStringSource().rememberString(),
                            fontSize = myTextSizes.sm,
                            color = myColors.onBackground / 60,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                        ) {
                            itemsIndexed(selected) { index, id ->
                                val label = optionsById[id] ?: id
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(myColors.onBackground / 5)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${index + 1}.",
                                        fontSize = myTextSizes.sm,
                                        color = myColors.onBackground / 60,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        label,
                                        modifier = Modifier.weight(1f),
                                        fontSize = myTextSizes.base,
                                    )
                                    MyIcon(
                                        MyIcons.up,
                                        null,
                                        Modifier
                                            .size(16.dp)
                                            .clickable(enabled = enabled) { move(index, -1) }
                                            .alpha(if (index > 0) 1f else 0.3f),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    MyIcon(
                                        MyIcons.down,
                                        null,
                                        Modifier
                                            .size(16.dp)
                                            .clickable(enabled = enabled) { move(index, 1) }
                                            .alpha(if (index < selected.lastIndex) 1f else 0.3f),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    MyIcon(
                                        MyIcons.remove,
                                        null,
                                        Modifier
                                            .size(16.dp)
                                            .clickable(enabled = enabled) { remove(index) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    @Composable
    private fun FlowRowItem(
        optionsById: Map<String, String>,
        selected: List<String>,
        enabled: Boolean,
        toggle: (String) -> Unit,
    ) {
        FlowRow(
            Modifier.fillMaxWidth()
        ) {
            optionsById.forEach { (id, label) ->
                val isSelected = id in selected
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .ifThen(isSelected) {
                            background(myColors.primary / 20)
                        }
                        .clickable(enabled = enabled) { toggle(id) }
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
}
