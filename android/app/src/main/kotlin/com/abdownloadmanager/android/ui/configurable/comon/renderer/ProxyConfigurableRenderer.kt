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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.ConfigurableSheet
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
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
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.downloader.connection.proxy.Proxy
import ir.amirab.downloader.connection.proxy.ProxyType
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
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
        var proxyConfigState by remember {
            mutableStateOf(null as ProxyEditState?)
        }
        val dismiss = {
            proxyConfigState = null
        }
        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable(
                    onClick = {
                        proxyConfigState = ProxyEditState(
                            proxyData = value,
                            setProxyData = {
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
        proxyConfigState?.let {
            ProxyEditDialog(it, onDismiss = dismiss)
        }
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
                ProxyMode.Manual -> {
                    val hostValid = proxyHost.value.isNotBlank()
                    hostValid
                }
                // at the moment these two not supported on android
                ProxyMode.UseSystem -> false
                ProxyMode.Pac -> false
//                ProxyMode.Pac -> {
//                    HttpUrlUtils.isValidUrl(pacURL.value)
//                }
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
    fun RenderChangeProxyConfig() {
        NextIcon()
    }


    @Composable
    private fun ProxyEditDialog(
        state: ProxyEditState?,
        onDismiss: () -> Unit,
    ) {
        val headerTitle = Res.string.proxy_change_title.asStringSource()
        ConfigurableSheet(
            title = headerTitle,
            onDismiss = onDismiss,
            isOpened = state != null,
            content = {
                state?.let { state ->
                    val (mode, setMode) = state.proxyMode

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
                                    }

                                    ProxyMode.Manual -> {
                                        Column(cm) {
                                            RenderManualConfig(state)
                                        }
                                    }

                                    ProxyMode.Pac -> {
//                                            Column(cm) {
//                                                RenderPACConfig(state)
//                                            }
                                    }
                                }
                            }
                        )
                        ProxyConfigSpacer()
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
            modifier = Modifier.Companion,
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
                        modifier = Modifier.width(120.dp),
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
                    modifier = Modifier.clickable {
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
            modifier = Modifier.Companion,
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


}
