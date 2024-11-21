package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.Configurable
import com.abdownloadmanager.utils.compose.WithContentAlpha
import com.abdownloadmanager.utils.compose.WithContentColor
import com.abdownloadmanager.utils.compose.widget.MyIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.desktop.utils.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberComponentRectPositionProvider


@Composable
fun <T> RenderSpinner(
    possibleValues: List<T>,
    value: T,
    onSelect: (T) -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
//    minWidth:Dp,
    render: @Composable (T) -> Unit,
) {
    val verticalPadding = 4.dp
    val horizontalPadding = 4.dp

    var isOpen by remember { mutableStateOf(false) }
    val shape = RectangleShape
    val borderWidth = 1.dp
    val borderColor = myColors.onBackground / 10
    var widthForPopup by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current
    Box {
        Row(
            modifier = modifier
//                .widthIn(min = minWidth)
                .clip(shape)
                .height(IntrinsicSize.Max)
                .onGloballyPositioned {
                    widthForPopup = with(density) {
                        it.size.width.toDp()
                    }
                }
                .background(myColors.surface)
                .border(borderWidth, borderColor, shape)
                .clickable(enabled = enabled) {
                    isOpen = true
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WithContentAlpha(1f) {
                Box(
                    Modifier
                        .padding(vertical = verticalPadding)
                        .padding(horizontal = horizontalPadding)
                ) {
                    render(value)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(
                        Modifier.fillMaxHeight()
                            .padding(vertical = borderWidth)
                            .width(borderWidth).background(myColors.onBackground / 10)
                    )
                    MyIcon(MyIcons.down, null, Modifier.padding(4.dp).size(12.dp))
                }
            }
        }
        if (isOpen) {
            Popup(
                popupPositionProvider = rememberComponentRectPositionProvider(),
                onDismissRequest = { isOpen = false },
            ) {
                Box {
                    val scrollState = rememberScrollState()
                    Column(
                        Modifier
                            .width(IntrinsicSize.Max)
                            .widthIn(widthForPopup)
                            .heightIn(max = 360.dp)
                            .background(myColors.surface)
                            .border(borderWidth, borderColor, shape)
                            .padding(borderWidth)
                            .verticalScroll(scrollState)
                    ) {
                        WithContentColor(myColors.onSurface) {
                            for (p in possibleValues) {
                                key(p) {
                                    Row(
                                        modifier = Modifier
                                            .clickable(onClick = {
                                                isOpen = false
                                                onSelect(p)
                                            }),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        val selected = p == value
                                        WithContentAlpha(if (selected) 1f else 0.75f) {
                                            Box(
                                                Modifier
                                                    .weight(1f)
                                                    .padding(vertical = verticalPadding)
                                                    .padding(horizontal = horizontalPadding)
                                            ) {
                                                render(p)
                                            }
                                        }
                                        Spacer(
                                            Modifier.width(borderWidth)
                                        )
                                        if (selected) {
                                            MyIcon(MyIcons.check, null, Modifier.padding(4.dp).size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    VerticalScrollbar(
                        rememberScrollbarAdapter(scrollState),
                        style = ScrollbarStyle(
                            hoverColor = myColors.onBackground / 10,
                            unhoverColor = myColors.onBackground / 5,
                            hoverDurationMillis = 300,
                            thickness = 8.dp,
                            shape = RectangleShape,
                            minimalHeight = 16.dp
                        ),
                        modifier = Modifier
                            .padding(vertical = borderWidth)
                            .matchParentSize().wrapContentWidth(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun <T> TitleAndDescription(
    cfg: Configurable<T>,
    describe: Boolean = true,
    modifier: Modifier = Modifier.padding(8.dp),
) {
    val enabled = isConfigEnabled()
    Column(
        modifier.ifThen(!enabled) {
            alpha(0.5f)
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                cfg.title.rememberString(),
                fontSize = myTextSizes.base,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, false)
            )
            if (cfg.description.rememberString().isNotBlank()) {
                Spacer(Modifier.size(4.dp))
                Help(
                    Modifier.align(Alignment.Top),
                    cfg
                )
            }
        }
        if (describe) {
            val value = cfg.backedBy.collectAsState().value
            val describedStringSource = remember(value) {
                cfg.describe(value)
            }
            val describeContent = describedStringSource.rememberString()
            if (describeContent.isNotBlank()) {
                WithContentAlpha(0.75f) {
                    Text(
                        describeContent,
                        fontSize = myTextSizes.base,
                    )
                }
            }
        }
    }
}

private val LocalConfigurableIsEnabled = compositionLocalOf<Boolean> {
    error("LocalConfigurableIsEnabled not provided")
}
private val LocalConfigurableIsVisible = compositionLocalOf<Boolean> {
    error("LocalConfigurableIsVisible not provided")
}

@Composable
fun isConfigVisible(): Boolean {
    return LocalConfigurableIsVisible.current
}

@Composable
fun isConfigEnabled(): Boolean {
    return LocalConfigurableIsEnabled.current
}

@Composable
fun ConfigurationWrapper(
    configurable: Configurable<*>,
    groupInfo: ConfigGroupInfo? = null,
    content: @Composable () -> Unit,
) {
    val enabled by configurable.enabled.collectAsState()
    val visible by configurable.visible.collectAsState()
    CompositionLocalProvider(
        LocalConfigurableIsEnabled provides (enabled && groupInfo?.enabled ?: true),
        LocalConfigurableIsVisible provides (visible && groupInfo?.visible ?: true),
    ) {
        AnimatedVisibility(
            visible = visible,
            exit = shrinkVertically(),
            enter = expandVertically(),
        ) {
            content()
        }
    }
}

@Composable
fun ConfigTemplate(
    modifier: Modifier,
    title: @Composable ColumnScope.() -> Unit,
    value: @Composable ColumnScope.() -> Unit,
    nestedContent: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier
    ) {
        Row(
            Modifier
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                Modifier.weight(1f, true),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                title()
            }
            Column(
                Modifier.fillMaxHeight().weight(1f, true),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
            ) {
                value()
            }
        }
        Column(
            Modifier.fillMaxWidth()
        ) {
            nestedContent()
        }
    }
}
//@Immutable
//data class ConfigurationStyle(
//    val size:Float,
//){
//    companion object{
//        val compact = ConfigurationStyle(0.5f)
//        val normal = ConfigurationStyle(1f)
//    }
//}
//
//val LocalConfigurableStyle = compositionLocalOf<ConfigurationStyle> {
//    ConfigurationStyle.normal
//}

@Composable
private fun Help(
    modifier: Modifier = Modifier,
    cfg: Configurable<*>,
) {
    var showHelpContent by remember { mutableStateOf(false) }
    val onRequestCloseShowHelpContent = {
        showHelpContent = false
    }
    Column(modifier) {
        MyIcon(
            MyIcons.question,
            "Hint",
            Modifier
                .clip(CircleShape)
                .clickable {
                    showHelpContent = !showHelpContent
                }
                .border(
                    1.dp,
                    if (showHelpContent) myColors.primary
                    else Color.Transparent,
                    CircleShape
                )
                .background(myColors.surface)
                .padding(4.dp)
                .size(12.dp),
            tint = myColors.onSurface,
        )
        if (showHelpContent) {
            Popup(
                popupPositionProvider = rememberComponentRectPositionProvider(
                    anchor = Alignment.TopCenter,
                    alignment = Alignment.TopCenter,
                ),
                onDismissRequest = onRequestCloseShowHelpContent
            ) {
                val shape = RoundedCornerShape(6.dp)
                Box(
                    Modifier
                        .padding(vertical = 4.dp)
                        .widthIn(max = 240.dp)
                        .shadow(24.dp)
                        .clip(shape)
                        .border(1.dp, myColors.surface, shape)
                        .background(myColors.menuGradientBackground)
                        .padding(8.dp)
                ) {
                    WithContentColor(myColors.onSurface) {
                        Text(
                            cfg.description.rememberString(),
                            fontSize = myTextSizes.base,
                        )
                    }
                }
            }
        }
    }
}