package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.configurable.item.ProxyConfigurable
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.ExpandableItem
import com.abdownloadmanager.shared.ui.widget.Help
import com.abdownloadmanager.shared.ui.widget.IntTextField
import com.abdownloadmanager.shared.ui.widget.Multiselect
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.ui.widget.RadioButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.proxy.ProxyData
import com.abdownloadmanager.shared.util.proxy.ProxyMode
import com.abdownloadmanager.shared.util.proxy.ProxyRules
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.downloader.connection.proxy.Proxy
import ir.amirab.downloader.connection.proxy.ProxyType
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.ifThen

object ProxyConfigurableRenderer : ConfigurableRenderer<ProxyConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: ProxyConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderProxyConfig(configurable, configurableUiProps)
    }


    @Composable
    fun RenderProxyConfig(cfg: ProxyConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
        val enabled = isConfigEnabled()
        ConfigTemplate(
            modifier = configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, true)
            },
            value = {
                RenderChangeProxyConfig(
                    proxyWithRules = value,
                    setProxyWithRules = { setValue(it) }
                )
            },
        )
    }

    @Stable
    private class ProxyEditState(
        private val proxyData: ProxyData,
        private val setProxyData: (ProxyData) -> Unit,
    ) {
        var proxyMode = mutableStateOf(proxyData.proxyMode)

        //pac
        var pacURL = mutableStateOf(proxyData.pac.uri)

        //manual
        var proxyType = mutableStateOf(proxyData.proxyWithRules.proxy.type)

        var proxyHost = mutableStateOf(proxyData.proxyWithRules.proxy.host)
        var proxyPort = mutableStateOf(proxyData.proxyWithRules.proxy.port)

        var useAuth = mutableStateOf(proxyData.proxyWithRules.proxy.username != null)
        var proxyUsername = mutableStateOf(proxyData.proxyWithRules.proxy.username.orEmpty())
        var proxyPassword = mutableStateOf(proxyData.proxyWithRules.proxy.password.orEmpty())

        var excludeURLPatterns = mutableStateOf(proxyData.proxyWithRules.rules.excludeURLPatterns.joinToString(" "))

        val canSave: Boolean by derivedStateOf {
            when (proxyMode.value) {
                ProxyMode.Direct -> true
                ProxyMode.UseSystem -> true
                ProxyMode.Manual -> {
                    val hostValid = proxyHost.value.isNotBlank()
                    hostValid
                }

                ProxyMode.Pac -> {
                    HttpUrlUtils.isValidUrl(pacURL.value)
                }
            }

        }

        fun save() {
            val useAuth = useAuth.value
            if (!canSave) {
                return
            }
            setProxyData(
                proxyData.copy(
                    proxyMode = proxyMode.value,
                    pac = proxyData.pac.copy(pacURL.value),
                    proxyWithRules = proxyData.proxyWithRules.copy(
                        proxy = Proxy(
                            type = proxyType.value,
                            host = proxyHost.value.trim(),
                            port = proxyPort.value,
                            username = proxyUsername.value.takeIf { it.isNotEmpty() && useAuth },
                            password = proxyPassword.value.takeIf { it.isNotEmpty() && useAuth },
                        ),
                        rules = ProxyRules(
                            excludeURLPatterns = excludeURLPatterns.value
                                .split(" ")
                                .map { it.trim() }
                                .filterNot { it.isEmpty() },
                        )
                    )
                )
            )
        }
    }

    @Composable
    fun RenderChangeProxyConfig(
        proxyWithRules: ProxyData,
        setProxyWithRules: (ProxyData) -> Unit,
    ) {
        var showProxyConfig by remember {
            mutableStateOf(false)
        }
        ActionButton(
            myStringResource(Res.string.change_proxy),
            onClick = {
                showProxyConfig = true
            },
        )
        if (showProxyConfig) {
            val dismiss = {
                showProxyConfig = false
            }
            val state = remember(setProxyWithRules) {
                ProxyEditState(
                    proxyData = proxyWithRules,
                    setProxyData = {
                        setProxyWithRules(it)
                        dismiss()
                    }
                )
            }
            ProxyEditDialog(state, onDismiss = dismiss)
        }
    }


    @Composable
    private fun ProxyEditDialog(
        state: ProxyEditState,
        onDismiss: () -> Unit,
    ) {
        Dialog(
            onDismissRequest = (onDismiss),
            content = {
                val (mode, setMode) = state.proxyMode
                SettingsDialog(
                    headerTitle = myStringResource(Res.string.proxy_change_title),
                    onDismiss = onDismiss,
                    content = {
                        val shape = myShapes.defaultRounded
                        Column(
                            Modifier.Companion
                                .verticalScroll(rememberScrollState())
                        ) {
                            Accordion(
                                wrapItem = { item, content ->
                                    val selected = item == mode
                                    Box(
                                        Modifier.Companion.ifThen(selected) {
                                            Modifier.Companion
                                                .clip(shape)
                                                .border(1.dp, myColors.onBackground / 0.15f, shape)
                                                .background(myColors.background / 25)
                                        }
                                    ) {
                                        content()
                                    }
                                },
                                possibleValues = ProxyMode.Companion.usableValues(),
                                selectedItem = mode,
                                renderHeader = {
                                    val selected = it == mode
                                    Row(
                                        Modifier.Companion
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
                                        Spacer(Modifier.Companion.width(8.dp))
                                        Text(
                                            text = it.asStringSource().rememberString(),
                                            fontSize = if (selected) {
                                                myTextSizes.lg
                                            } else {
                                                myTextSizes.base
                                            },
                                            fontWeight = if (selected) {
                                                FontWeight.Companion.Bold
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                },
                                renderContent = {
                                    val cm = Modifier.Companion
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 12.dp,
                                            horizontal = 16.dp
                                        )
                                    when (it) {
                                        ProxyMode.Direct -> {

                                        }

                                        ProxyMode.UseSystem -> {
                                            Column(cm) {
                                                ActionButton(
                                                    myStringResource(Res.string.proxy_open_system_proxy_settings),
                                                    onClick = {
                                                        DesktopUtils.Companion.openSystemProxySettings()
                                                    },
                                                )
                                            }
                                        }

                                        ProxyMode.Manual -> {
                                            Column(cm) {
                                                RenderManualConfig(state)
                                            }
                                        }

                                        ProxyMode.Pac -> {
                                            Column(cm) {
                                                RenderPACConfig(state)
                                            }
                                        }
                                    }
                                }
                            )
                            ProxyConfigSpacer()
                        }
                    },
                    actions = {
                        ActionButton(
                            myStringResource(Res.string.change),
                            enabled = state.canSave,
                            onClick = {
                                state.save()
                            })
                        Spacer(Modifier.Companion.width(8.dp))
                        ActionButton(myStringResource(Res.string.cancel), onClick = {
                            onDismiss()
                        })
                    }
                )
            }
        )
    }

    @Composable
    private fun RenderPACConfig(
        state: ProxyEditState,
    ) {
        Column {
            val (url, setPacUrl) = state.pacURL
            DialogConfigItem(
                modifier = Modifier.Companion,
                title = {
                    Text(myStringResource(Res.string.proxy_pac_url))
                },
                value = {
                    Row(
                        verticalAlignment = Alignment.Companion.CenterVertically,
                    ) {
                        MyTextField(
                            text = url,
                            onTextChange = setPacUrl,
                            placeholder = "http://path/to/file.pac",
                            modifier = Modifier.Companion.weight(1f),
                        )
                    }
                }
            )
        }
    }

    @Composable
    private fun RenderManualConfig(
        state: ProxyEditState,
    ) {
        val (type, setType) = state.proxyType
        val (host, setHost) = state.proxyHost
        val (port, setPort) = state.proxyPort
        val (useAuth, setUseAuth) = state.useAuth
        val (username, setUsername) = state.proxyUsername
        val (password, setPassword) = state.proxyPassword
        val (excludeURLPatterns, setExcludeURLPatterns) = state.excludeURLPatterns
        DialogConfigItem(
            modifier = Modifier.Companion,
            title = {
                Text(myStringResource(Res.string.proxy_type))
            },
            value = {
                Multiselect(
                    selections = ProxyType.entries.toList(),
                    selectedItem = type,
                    onSelectionChange = setType,
                    modifier = Modifier.Companion,
                    render = {
                        Text(
                            it.name,
                            modifier = Modifier.Companion.padding(vertical = 4.dp, horizontal = 8.dp),
                        )
                    },
                    selectedColor = LocalContentColor.current / 15,
                    unselectedAlpha = 0.8f,
                )
            }
        )
        ProxyConfigSpacer()
        DialogConfigItem(
            modifier = Modifier.Companion,
            title = {
                Text(myStringResource(Res.string.address_and_port))
            },
            value = {
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    MyTextField(
                        text = host,
                        onTextChange = setHost,
                        placeholder = "127.0.0.1",
                        modifier = Modifier.Companion.weight(1f),
                    )
                    Text(":", Modifier.Companion.padding(horizontal = 8.dp))
                    IntTextField(
                        value = port,
                        onValueChange = setPort,
                        placeholder = myStringResource(Res.string.port),
                        range = 1..65535,
                        modifier = Modifier.Companion.width(96.dp),
                        keyboardOptions = KeyboardOptions(),
                        textPadding = PaddingValues(8.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
        )
        ProxyConfigSpacer()
        DialogConfigItem(
            modifier = Modifier.Companion,
            title = {
                Row(
                    modifier = Modifier.Companion.onClick {
                        setUseAuth(!useAuth)
                    }
                ) {
                    CheckBox(
                        value = useAuth,
                        onValueChange = setUseAuth,
                        size = 16.dp
                    )
                    Spacer(Modifier.Companion.width(8.dp))
                    Text(myStringResource(Res.string.use_authentication))
                }
            },
            value = {
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    MyTextField(
                        text = username,
                        onTextChange = setUsername,
                        placeholder = myStringResource(Res.string.username),
                        modifier = Modifier.Companion.weight(1f),
                        enabled = useAuth,
                    )
                    Spacer(Modifier.Companion.width(8.dp))
                    MyTextField(
                        text = password,
                        onTextChange = setPassword,
                        placeholder = myStringResource(Res.string.password),
                        modifier = Modifier.Companion.weight(1f),
                        enabled = useAuth,
                    )
                }
            }
        )
        ProxyConfigSpacer()
        DialogConfigItem(
            modifier = Modifier.Companion,
            title = {
                Row {
                    Text(myStringResource(Res.string.proxy_do_not_use_proxy_for))
                    Spacer(Modifier.Companion.width(8.dp))
                    Help(
                        myStringResource(Res.string.proxy_do_not_use_proxy_for_description)
                    )
                }
            },
            value = {
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    MyTextField(
                        text = excludeURLPatterns,
                        onTextChange = setExcludeURLPatterns,
                        placeholder = "example.com 192.168.1.*",
                        modifier = Modifier.Companion,
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
            modifier = Modifier.Companion
                .clip(shape)
                .border(2.dp, myColors.onBackground / 10, shape)
                .background(
                    Brush.Companion.linearGradient(
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
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    headerTitle,
                    fontSize = myTextSizes.lg,
                    fontWeight = FontWeight.Companion.Bold,
                )
                MyIcon(
                    MyIcons.windowClose,
                    myStringResource(Res.string.close),
                    Modifier.Companion
                        .clip(CircleShape)
                        .clickable { onDismiss() }
                        .padding(12.dp)
                        .size(12.dp),
                )
            }
            Spacer(Modifier.Companion.height(8.dp))
            Box(Modifier.Companion.weight(1f, false)) {
                content()
            }
            actions?.let {
                Spacer(Modifier.Companion.height(8.dp))
                Row(
                    Modifier.Companion.align(Alignment.Companion.End),
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    actions()
                }
            }
        }
    }

    @Composable
    private fun ProxyConfigSpacer() {
        Spacer(Modifier.Companion.height(8.dp))
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
                Modifier.Companion
                    .height(IntrinsicSize.Max),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.Start,
                ) {
                    title()
                }
                Spacer(Modifier.Companion.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.End,
                ) {
                    value()
                }
            }
        }
    }

    private fun ProxyMode.asStringSource(): StringSource {
        return when (this) {
            ProxyMode.Direct -> Res.string.proxy_no
            ProxyMode.UseSystem -> Res.string.proxy_system
            ProxyMode.Manual -> Res.string.proxy_manual
            ProxyMode.Pac -> Res.string.proxy_pac
        }.asStringSource()
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
