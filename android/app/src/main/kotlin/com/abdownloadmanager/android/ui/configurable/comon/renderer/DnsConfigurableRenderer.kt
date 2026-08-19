package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.ConfigurableSheet
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.storage.DnsSettings
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.DnsConfigurable
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.ExpandableItem
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.ui.widget.RadioButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.dns.DNSOption
import com.abdownloadmanager.shared.util.dns.DnsModes
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen

object DnsConfigurableRenderer : ConfigurableRenderer<DnsConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: DnsConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderDnsConfig(configurable, configurableUiProps)
    }

    @Composable
    fun RenderDnsConfig(cfg: DnsConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
        var dnsConfigState by remember {
            mutableStateOf(null as DnsEditState?)
        }
        val dismiss = {
            dnsConfigState = null
        }
        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable(
                    onClick = {
                        dnsConfigState = DnsEditState(
                            dnsSettings = value,
                            setDnsSettings = {
                                setValue(it)
                                dismiss()
                            }
                        )
                    }
                )
                .padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, true)
            },
            value = {
                NextIcon()
            }
        )
        dnsConfigState?.let {
            DnsEditDialog(it, onDismiss = dismiss)
        }
    }

    @Stable
    private class DnsEditState(
        private val dnsSettings: DnsSettings,
        private val setDnsSettings: (DnsSettings) -> Unit,
    ) {
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
    private fun DnsEditDialog(
        state: DnsEditState?,
        onDismiss: () -> Unit,
    ) {
        ConfigurableSheet(
            title = Res.string.settings_dns.asStringSource(),
            onDismiss = onDismiss,
            isOpened = state != null,
            content = {
                state?.let { state ->
                    val (mode, setMode) = state.mode
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
                                                if (selected) 8.dp else 4.dp
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
                        Row {
                            val btnModifier = Modifier.weight(1f)
                            ActionButton(
                                myStringResource(Res.string.change),
                                enabled = state.canSave,
                                modifier = btnModifier,
                                onClick = {
                                    state.save()
                                })
                            Spacer(Modifier.width(mySpacings.mediumSpace))
                            ActionButton(
                                myStringResource(Res.string.cancel),
                                modifier = btnModifier,
                                onClick = {
                                    onDismiss()
                                })
                        }
                    }
                }
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
