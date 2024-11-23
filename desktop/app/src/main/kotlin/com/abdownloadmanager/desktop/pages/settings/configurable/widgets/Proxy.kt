package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.abdownloadmanager.desktop.pages.settings.configurable.ProxyConfigurable
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.WindowClose
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.*
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.compose.LocalContentColor
import com.abdownloadmanager.utils.compose.widget.Icon
import com.abdownloadmanager.utils.proxy.ProxyMode
import com.abdownloadmanager.utils.proxy.ProxyRules
import com.abdownloadmanager.utils.proxy.ProxyWithRules
import ir.amirab.downloader.connection.proxy.Proxy
import ir.amirab.downloader.connection.proxy.ProxyType
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.DesktopUtils


@Composable
fun RenderProxyConfig(cfg: ProxyConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val enabled = isConfigEnabled()
    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, true)
        },
        value = {
            RenderSpinner(
                enabled = enabled,
                possibleValues = ProxyMode.usableValues(),
                value = value.proxyMode,
                onSelect = {
                    setValue(
                        value.copy(
                            proxyMode = it
                        )
                    )
                },
                modifier = Modifier.widthIn(min = 120.dp),
                render = {
                    val text = myStringResource(
                        when (it) {
                            ProxyMode.Direct -> Res.string.proxy_no
                            ProxyMode.UseSystem -> Res.string.proxy_system
                            ProxyMode.Manual -> Res.string.proxy_manual
                        }
                    )
                    Text(text)
                },
            )
        },
        nestedContent = {
            AnimatedContent(value.proxyMode.takeIf { enabled }) {
                when (it) {
                    ProxyMode.Direct -> {}
                    ProxyMode.UseSystem -> {
                        ActionButton(
                            myStringResource(Res.string.proxy_open_system_proxy_settings),
                            onClick = {
                                DesktopUtils.openSystemProxySettings()
                            },
                        )
                    }

                    ProxyMode.Manual -> {
                        RenderManualProxyConfig(
                            proxyWithRules = value.proxyWithRules,
                            setProxyWithRules = {
                                setValue(
                                    value.copy(
                                        proxyWithRules = it
                                    )
                                )
                            }
                        )
                    }

                    null -> {}
                }
            }
        }
    )
}

@Stable
private class ProxyEditState(
    private val proxyWithRules: ProxyWithRules,
    private val setProxyWithRules: (ProxyWithRules) -> Unit,
) {
    var proxyType = mutableStateOf(proxyWithRules.proxy.type)

    var proxyHost = mutableStateOf(proxyWithRules.proxy.host)
    var proxyPort = mutableStateOf(proxyWithRules.proxy.port)

    var useAuth = mutableStateOf(proxyWithRules.proxy.username != null)
    var proxyUsername = mutableStateOf(proxyWithRules.proxy.username.orEmpty())
    var proxyPassword = mutableStateOf(proxyWithRules.proxy.password.orEmpty())

    var excludeURLPatterns = mutableStateOf(proxyWithRules.rules.excludeURLPatterns.joinToString(" "))

    val canSave: Boolean by derivedStateOf {
        val hostValid = proxyHost.value.isNotBlank()
        hostValid
    }

    fun save() {
        val useAuth = useAuth.value
        if (!canSave) {
            return
        }
        setProxyWithRules(
            proxyWithRules.copy(
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
    }
}

@Composable
fun RenderManualProxyConfig(
    proxyWithRules: ProxyWithRules,
    setProxyWithRules: (ProxyWithRules) -> Unit,
) {
    var showManualProxyConfig by remember {
        mutableStateOf(false)
    }
    ActionButton(
        myStringResource(Res.string.change_proxy),
        onClick = {
            showManualProxyConfig = true
        },
    )
    if (showManualProxyConfig) {
        val dismiss = {
            showManualProxyConfig = false
        }
        val state = remember(setProxyWithRules) {
            ProxyEditState(
                proxyWithRules = proxyWithRules,
                setProxyWithRules = {
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
            val (type, setType) = state.proxyType
            val (host, setHost) = state.proxyHost
            val (port, setPort) = state.proxyPort
            val (useAuth, setUseAuth) = state.useAuth
            val (username, setUsername) = state.proxyUsername
            val (password, setPassword) = state.proxyPassword
            val (excludeURLPatterns, setExcludeURLPatterns) = state.excludeURLPatterns

            SettingsDialog(
                headerTitle = myStringResource(Res.string.proxy_change_title),
                onDismiss = onDismiss,
                content = {
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                    ) {
                        val spacer = @Composable {
                            Spacer(Modifier.height(8.dp))
                        }
                        DialogConfigItem(
                            modifier = Modifier,
                            title = {
                                Text(myStringResource(Res.string.proxy_type))
                            },
                            value = {
                                Multiselect(
                                    selections = ProxyType.entries.toList(),
                                    selectedItem = type,
                                    onSelectionChange = setType,
                                    modifier = Modifier,
                                    render = {
                                        Text(
                                            it.name,
                                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                                        )
                                    },
                                    selectedColor = LocalContentColor.current / 15,
                                    unselectedAlpha = 0.8f,
                                )
                            }
                        )
                        spacer()
                        DialogConfigItem(
                            modifier = Modifier,
                            title = {
                                Text(myStringResource(Res.string.address_and_port))
                            },
                            value = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MyTextField(
                                        text = host,
                                        onTextChange = setHost,
                                        placeholder = "127.0.0.1",
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(":", Modifier.padding(horizontal = 8.dp))
                                    IntTextField(
                                        value = port,
                                        onValueChange = setPort,
                                        placeholder = myStringResource(Res.string.port),
                                        range = 1..65535,
                                        modifier = Modifier.width(96.dp),
                                        keyboardOptions = KeyboardOptions(),
                                        textPadding = PaddingValues(8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                }
                            }
                        )
                        spacer()
                        DialogConfigItem(
                            modifier = Modifier,
                            title = {
                                Row(
                                    modifier = Modifier.onClick {
                                        setUseAuth(!useAuth)
                                    }
                                ) {
                                    CheckBox(
                                        value = useAuth,
                                        onValueChange = setUseAuth,
                                        size = 16.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(myStringResource(Res.string.use_authentication))
                                }
                            },
                            value = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MyTextField(
                                        text = username,
                                        onTextChange = setUsername,
                                        placeholder = myStringResource(Res.string.username),
                                        modifier = Modifier.weight(1f),
                                        enabled = useAuth,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    MyTextField(
                                        text = password,
                                        onTextChange = setPassword,
                                        placeholder = myStringResource(Res.string.password),
                                        modifier = Modifier.weight(1f),
                                        enabled = useAuth,
                                    )
                                }
                            }
                        )
                        spacer()
                        DialogConfigItem(
                            modifier = Modifier,
                            title = {
                                Row {
                                    Text(myStringResource(Res.string.proxy_do_not_use_proxy_for))
                                    Spacer(Modifier.width(8.dp))
                                    Help(
                                        myStringResource(Res.string.proxy_do_not_use_proxy_for_description)
                                    )
                                }
                            },
                            value = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MyTextField(
                                        text = excludeURLPatterns,
                                        onTextChange = setExcludeURLPatterns,
                                        placeholder = "example.com 192.168.1.*",
                                        modifier = Modifier,
                                    )
                                }
                            }
                        )
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
private fun SettingsDialog(
    headerTitle: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(6.dp)
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
            Icon(
                imageVector = AbIcons.Default.WindowClose,
                contentDescription = myStringResource(Res.string.close),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onDismiss() }
                    .padding(12.dp)
                    .size(12.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        content()
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