package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.storage.DnsSettings
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.DnsConfigurable
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.dns.DNSOption
import com.abdownloadmanager.shared.util.dns.DnsModes
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen

object DnsConfigurableRenderer : ConfigurableRenderer<DnsConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: DnsConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderProxyConfig(configurable, configurableUiProps)
    }


    @Composable
    fun RenderProxyConfig(cfg: DnsConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
        ConfigTemplate(
            modifier = configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, true)
            },
            value = {
                RenderChangeDnsConfig(
                    dnsSettings = value,
                    setDnsSettings = { setValue(it) }
                )
            },
        )
    }

    @Stable
    private class DnsEditState(
        private val dnsSettings: DnsSettings,
        private val setDnsSettings: (DnsSettings) -> Unit,
    ) {
        //pac
        var mode = mutableStateOf(dnsSettings.mode)
        var address = mutableStateOf(dnsSettings.address)

        val canSave: Boolean by derivedStateOf {
            when (mode.value) {
                DnsModes.System -> true
                DnsModes.DnsOverHttps -> DNSOption.DnsOverHttps.isValid(address.value)
            }
        }

        fun save() {
            if (!canSave) {
                return
            }
            setDnsSettings(
                dnsSettings.copy(
                    mode = mode.value,
                    address = address.value
                )
            )
        }
    }

    @Composable
    fun RenderChangeDnsConfig(
        dnsSettings: DnsSettings,
        setDnsSettings: (DnsSettings) -> Unit,
    ) {
        var showDnsConfig by remember {
            mutableStateOf(false)
        }
        ActionButton(
            myStringResource(Res.string.change),
            onClick = {
                showDnsConfig = true
            },
        )
        if (showDnsConfig) {
            val dismiss = {
                showDnsConfig = false
            }
            val state = remember(setDnsSettings) {
                DnsEditState(
                    dnsSettings = dnsSettings,
                    setDnsSettings = {
                        setDnsSettings(it)
                        dismiss()
                    }
                )
            }
            DnsEditDialog(state, onDismiss = dismiss)
        }
    }


    @Composable
    private fun DnsEditDialog(
        state: DnsEditState,
        onDismiss: () -> Unit,
    ) {
        Dialog(
            onDismissRequest = (onDismiss),
            content = {
                val (mode, setMode) = state.mode
                SettingsDialog(
                    headerTitle = myStringResource(Res.string.settings_dns),
                    onDismiss = onDismiss,
                    content = {
                        val shape = myShapes.defaultRounded
                        Column(
                            Modifier
                                .verticalScroll(rememberScrollState())
                        ) {
                            Accordion(
                                wrapItem = { item, content ->
                                    val selected = item == mode
                                    Box(
                                        Modifier.ifThen(selected) {
                                            Modifier
                                                .clip(shape)
                                                .border(1.dp, myColors.onBackground / 0.15f, shape)
                                                .background(myColors.background / 25)
                                        }
                                    ) {
                                        content()
                                    }
                                },
                                possibleValues = DnsModes.entries,
                                selectedItem = mode,
                                renderHeader = {
                                    val selected = it == mode
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(shape)
                                            .clickable { setMode(it) }
                                            .padding(8.dp)
                                            .padding(
                                                animateDpAsState(
                                                    if (selected) 4.dp else 0.dp
                                                ).value
                                            )
                                    ) {
                                        RadioButton(
                                            value = selected,
                                            onValueChange = {},
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = it.stringSource.rememberString(),
                                            fontSize = if (selected) {
                                                myTextSizes.lg
                                            } else {
                                                myTextSizes.base
                                            },
                                            fontWeight = if (selected) {
                                                FontWeight.Bold
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                },
                                renderContent = {
                                    val cm = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 12.dp,
                                            horizontal = 16.dp
                                        )
                                    when (it) {
                                        DnsModes.System -> {
                                        }

                                        DnsModes.DnsOverHttps -> {
                                            Column(cm) {
                                                RenderDOHConfig(state)
                                            }
                                        }
                                    }
                                }
                            )
                            DnsConfigSpacer()
                        }
                    },
                    actions = {
                        ActionButton(
                            myStringResource(Res.string.change),
                            enabled = state.canSave,
                            onClick = {
                                state.save()
                            })
                        Spacer(Modifier.width(8.dp))
                        ActionButton(myStringResource(Res.string.cancel), onClick = {
                            onDismiss()
                        })
                    }
                )
            }
        )
    }

    @Composable
    private fun RenderDOHConfig(
        state: DnsEditState,
    ) {
        val (address, setAddress) = state.address
        DnsConfigSpacer()
        DialogConfigItem(
            modifier = Modifier.Companion,
            title = {
                Text(myStringResource(Res.string.address))
            },
            value = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MyTextField(
                        text = address,
                        onTextChange = setAddress,
                        placeholder = "https://1.1.1.1/dns-query",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        )
    }

    @Composable
    private fun SettingsDialog(
        headerTitle: String,
        onDismiss: () -> Unit,
        content: @Composable () -> Unit,
        actions: (@Composable RowScope.() -> Unit)? = null,
    ) {
        val shape = myShapes.defaultRounded
        Column(
            modifier = Modifier
                .clip(shape)
                .border(2.dp, myColors.onBackground / 10, shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            myColors.surface,
                            myColors.background,
                        )
                    )
                )
                .padding(16.dp)
                .width(450.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    headerTitle,
                    fontSize = myTextSizes.lg,
                    fontWeight = FontWeight.Bold,
                )
                MyIcon(
                    MyIcons.windowClose,
                    myStringResource(Res.string.close),
                    Modifier
                        .clip(CircleShape)
                        .clickable { onDismiss() }
                        .padding(12.dp)
                        .size(12.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f, false)) {
                content()
            }
            actions?.let {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions()
                }
            }
        }
    }

    @Composable
    private fun DnsConfigSpacer() {
        Spacer(Modifier.height(8.dp))
    }

    @Composable
    private fun DialogConfigItem(
        modifier: Modifier,
        title: @Composable ColumnScope.() -> Unit,
        value: @Composable ColumnScope.() -> Unit,
    ) {
        Column(
            modifier,
        ) {
            Column(
                Modifier
                    .height(IntrinsicSize.Max),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    title()
                }
                Spacer(Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End,
                ) {
                    value()
                }
            }
        }
    }

    @Composable
    private fun <T> Accordion(
        possibleValues: List<T>,
        selectedItem: T,
        wrapItem: @Composable (T, @Composable () -> Unit) -> Unit = { _, content -> content() },
        renderHeader: @Composable (T) -> Unit,
        renderContent: @Composable (T) -> Unit,
    ) {
        Column {
            possibleValues.forEach {
                wrapItem(it) {
                    ExpandableItem(
                        isExpanded = selectedItem == it,
                        header = {
                            renderHeader(it)
                        },
                        body = {
                            renderContent(it)
                        },
                    )
                }
            }
        }
    }


}
