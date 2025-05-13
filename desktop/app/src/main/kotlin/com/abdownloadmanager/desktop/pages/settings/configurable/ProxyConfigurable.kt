package com.abdownloadmanager.desktop.pages.settings.configurable

import androidx.compose.animation.core.animateDpAsState
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
import com.abdownloadmanager.desktop.utils.configurable.Configurable
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.proxy.*
import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import ir.amirab.downloader.connection.proxy.Proxy
import ir.amirab.downloader.connection.proxy.ProxyType
import ir.amirab.util.UrlUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.ifThen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProxyConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<ProxyData>,
    describe: (ProxyData) -> StringSource,
    validate: (ProxyData) -> Boolean,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<ProxyData>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    validate = validate,
    enabled = enabled,
    visible = visible,
) {
    @Composable
    override fun render(modifier: Modifier) {
        RenderProxyConfig(this, modifier)
    }
}


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
                UrlUtils.isValidUrl(pacURL.value)
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
                    val shape = RoundedCornerShape(6.dp)
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
                            possibleValues = ProxyMode.usableValues(),
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
                                        text = it.asStringSource().rememberString(),
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
                                    ProxyMode.Direct -> {

                                    }

                                    ProxyMode.UseSystem -> {
                                        Column(cm) {
                                            ActionButton(
                                                myStringResource(Res.string.proxy_open_system_proxy_settings),
                                                onClick = {
                                                    DesktopUtils.openSystemProxySettings()
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
private fun RenderPACConfig(
    state: ProxyEditState,
) {
    Column {
        val (url, setPacUrl) = state.pacURL
        DialogConfigItem(
            modifier = Modifier,
            title = {
                Text(myStringResource(Res.string.proxy_pac_url))
            },
            value = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MyTextField(
                        text = url,
                        onTextChange = setPacUrl,
                        placeholder = "http://path/to/file.pac",
                        modifier = Modifier.weight(1f),
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
    ProxyConfigSpacer()
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
    ProxyConfigSpacer()
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
    ProxyConfigSpacer()
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
private fun ProxyConfigSpacer() {
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

