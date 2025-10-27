package com.abdownloadmanager.shared.ui.configurable

import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.abdownloadmanager.shared.ui.widget.Help
import com.abdownloadmanager.shared.ui.widget.rememberMyComponentRectPositionProvider
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.MultiplatformVerticalScrollbar
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
fun <T> defaultValueToString(item: T): List<String> {
    return emptyList()
}

private const val SEARCH_RESET_TIMEOUT = 2_000L
private fun Modifier.onSearch(
    searchDelayTimeout: Long = SEARCH_RESET_TIMEOUT,
    onSearchRequested: (String) -> Unit
): Modifier {
    return composed {
        var textToSearch by remember {
            mutableStateOf("")
        }
        LaunchedEffect(textToSearch) {
            if (textToSearch.isNotEmpty()) {
                onSearchRequested(textToSearch)
                delay(searchDelayTimeout)
                textToSearch = ""
            }
        }
        onKeyEvent {
            if (it.type == KeyEventType.KeyDown) {
                val char = it.utf16CodePoint.toChar()
                if (char.isLetterOrDigit()) {
                    textToSearch += char
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }
}

@Composable
fun <T> RenderSpinner(
    possibleValues: List<T>,
    value: T,
    onSelect: (T) -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    valueToString: (T) -> List<String> = ::defaultValueToString,
//    minWidth:Dp,
    render: @Composable (T) -> Unit,
) {
    val verticalPadding = 4.dp
    val horizontalPadding = 4.dp

    var isOpen by remember { mutableStateOf(false) }
    val shape = myShapes.defaultRounded
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
                .heightIn(mySpacings.thumbSize)
                .clickable(enabled = enabled) {
                    isOpen = true
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
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
                popupPositionProvider = rememberMyComponentRectPositionProvider(
                    offset = DpOffset(y = 2.dp, x = 0.dp)
                ),
                onDismissRequest = { isOpen = false },
                properties = PopupProperties(
                    focusable = true
                )
            ) {
                val coroutineScope = rememberCoroutineScope()
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                val possibleValuePositions = remember(possibleValues) {
                    mutableStateMapOf<Int, Float>()
                }
                var itemToBeIndicated: Int by remember {
                    mutableStateOf(-1)
                }
                LaunchedEffect(itemToBeIndicated) {
                    if (itemToBeIndicated != -1) {
                        delay(SEARCH_RESET_TIMEOUT)
                        itemToBeIndicated = -1
                    }
                }
                Box {
                    val scrollState = rememberScrollState()
                    Column(
                        Modifier
                            .clip(shape)
                            .width(IntrinsicSize.Max)
                            .widthIn(widthForPopup)
                            .heightIn(max = 360.dp)
                            .onSearch { searchText ->
                                val itemIndex = possibleValues
                                    .indexOfFirst { value ->
                                        valueToString(value).any { string ->
                                            string.startsWith(
                                                searchText,
                                                ignoreCase = true,
                                            )
                                        }
                                    }
                                if (itemIndex == -1) {
                                    return@onSearch
                                }
                                val position = possibleValuePositions[itemIndex]?.roundToInt()
                                coroutineScope.launch {
                                    position?.let {
                                        scrollState.scrollTo(it)
                                        itemToBeIndicated = itemIndex
                                    }
                                }
                            }
                            .focusRequester(focusRequester)
                            .focusable()
                            .background(myColors.surface)
                            .border(borderWidth, borderColor, shape)
                            .padding(borderWidth)
                            .clip(shape)
                            .verticalScroll(scrollState)
                    ) {
                        WithContentColor(myColors.onSurface) {
                            for ((index, p) in possibleValues.withIndex()) {
                                key(p) {
                                    val isIndicating = itemToBeIndicated == index
                                    Row(
                                        modifier = Modifier
                                            .onGloballyPositioned {
                                                possibleValuePositions[index] = it.positionInParent().y
                                            }
                                            .ifThen(isIndicating) {
                                                background(
                                                    myColors.onBackground / 0.05f
                                                )
                                            }
                                            .heightIn(mySpacings.thumbSize)
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
                    MultiplatformVerticalScrollbar(
                        rememberScrollbarAdapter(scrollState),
                        modifier = Modifier
                            .padding(vertical = borderWidth)
                            .matchParentSize().wrapContentWidth(Alignment.End)
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
fun Help(
    modifier: Modifier = Modifier,
    cfg: Configurable<*>,
) {
    Help(cfg.description.rememberString(), modifier)
}
