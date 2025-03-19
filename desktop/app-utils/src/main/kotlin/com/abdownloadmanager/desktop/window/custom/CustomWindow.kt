package com.abdownloadmanager.desktop.window.custom

import ir.amirab.util.customwindow.ProvideWindowSpotContainer
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import com.abdownloadmanager.shared.utils.ui.WithContentColor
import ir.amirab.util.compose.IconSource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.utils.PopUpContainer
import com.abdownloadmanager.shared.utils.ResponsiveBox
import com.abdownloadmanager.shared.utils.ui.WithTitleBarDirection
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.UiScaledContent
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import ir.amirab.util.desktop.LocalWindow
import ir.amirab.util.customwindow.HitSpots
import ir.amirab.util.customwindow.util.CustomWindowDecorationAccessing
import ir.amirab.util.customwindow.windowFrameItem
import ir.amirab.util.platform.Platform
import ir.amirab.util.ifThen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


// a window frame which totally rendered with compose
@Composable
private fun FrameWindowScope.CustomWindowFrame(
    onRequestMinimize: (() -> Unit)?,
    onRequestClose: () -> Unit,
    onRequestToggleMaximize: (() -> Unit)?,
    title: String,
    titlePosition: TitlePosition,
    windowIcon: Painter? = null,
    background: Color,
    onBackground: Color,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
//    val borderColor = MaterialTheme.colors.surface
    WithContentColor(onBackground) {
        Column(
            Modifier
                .fillMaxSize()
                .ifThen(!CustomWindowDecorationAccessing.isSupported) {
                    border(1.dp, Color.Gray.copy(0.25f), RectangleShape)
                        .padding(1.dp)
                }
                .background(background)
        ) {
            WithTitleBarDirection {
                SnapDraggableToolbar(
                    title = title,
                    windowIcon = windowIcon,
                    titlePosition = titlePosition,
                    start = start,
                    end = end,
                    onRequestMinimize = onRequestMinimize,
                    onRequestClose = onRequestClose,
                    onRequestToggleMaximize = onRequestToggleMaximize
                )
            }

            content()
        }
    }
}

@Composable
fun isWindowFocused(): Boolean {
    return LocalWindowInfo.current.isWindowFocused
}

@Composable
fun isWindowMaximized(): Boolean {
    return LocalWindowState.current.placement == WindowPlacement.Maximized
}

@Composable
fun isWindowFloating(): Boolean {
    return LocalWindowState.current.placement == WindowPlacement.Floating
}

@Composable
fun FrameWindowScope.SnapDraggableToolbar(
    title: String,
    windowIcon: Painter? = null,
    titlePosition: TitlePosition,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
) {
    ProvideWindowSpotContainer {
        if (CustomWindowDecorationAccessing.isSupported) {
            FrameContent(
                title = title,
                windowIcon = windowIcon,
                titlePosition = titlePosition,
                start = start,
                end = end,
                onRequestMinimize = onRequestMinimize,
                onRequestToggleMaximize = onRequestToggleMaximize,
                onRequestClose = onRequestClose
            )
        } else {
            WindowDraggableArea(
                Modifier.onClick(
                    onDoubleClick = {
                        onRequestToggleMaximize?.invoke()
                    },
                    onClick = {}
                )
            ) {
                FrameContent(
                    title = title,
                    windowIcon = windowIcon,
                    titlePosition = titlePosition,
                    start = start,
                    end = end,
                    onRequestMinimize = onRequestMinimize,
                    onRequestToggleMaximize = onRequestToggleMaximize,
                    onRequestClose = onRequestClose
                )
            }
        }
    }
}

@Composable
private fun FrameWindowScope.FrameContent(
    title: String,
    windowIcon: Painter? = null,
    titlePosition: TitlePosition,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val isMacOS = Platform.getCurrentPlatform() == Platform.Desktop.MacOS
        val startPadding = if (isMacOS) 76.dp else 16.dp

        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .fillMaxHeight()
                    .windowFrameItem("icon", HitSpots.MENU_BAR),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(startPadding))
                windowIcon?.let {
                    WithContentAlpha(1f) {
                        Image(it, null, Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }
            if (!titlePosition.afterStart) {
                Title(
                    modifier = Modifier
                        .ifThen(titlePosition.centered) {
                            weight(1f)
                                .ifThen(start == null) {
                                    wrapContentWidth()
                                }
                        }
                        .padding(titlePosition.padding),
                    title = title
                )
            }
            start?.let {
                Row(
                    Modifier.windowFrameItem("start", HitSpots.OTHER_HIT_SPOT)
                ) {
                    start()
                    Spacer(Modifier.width(8.dp))
                }
            }
            if (titlePosition.afterStart) {
                Title(
                    modifier = Modifier
                        .weight(1f)
                        .ifThen(titlePosition.centered) {
                            wrapContentWidth()
                        }
                        .padding(titlePosition.padding),
                    title = title
                )
            }
            if (!titlePosition.centered && !titlePosition.afterStart) {
                Spacer(Modifier.weight(1f))
            }
            end?.let {
                Row(
                    Modifier.windowFrameItem("end", HitSpots.OTHER_HIT_SPOT)
                ) {
                    end()
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
        if (Platform.getCurrentPlatform() != Platform.Desktop.MacOS) {
            WindowsActionButtons(
                onRequestClose,
                onRequestMinimize,
                onRequestToggleMaximize,
            )
        }
    }
}

@Composable
private fun FrameWindowScope.Title(
    modifier: Modifier, title: String,
) {
    WithContentColor(myColors.onBackground) {
        WithContentAlpha(1f) {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = myTextSizes.base,
                modifier = Modifier
                    .windowFrameItem("title", HitSpots.DRAGGABLE_AREA)
                    .then(modifier)
            )
        }
    }
}

private val defaultAppIcon: IconSource
    @Composable
    get() {
        return MyIcons.appIcon
    }

private fun Color.toWindowColorType() = java.awt.Color(
    red, green, blue
)

@Composable
fun CustomWindow(
    state: WindowState,
    onCloseRequest: () -> Unit,
    resizable: Boolean = true,
    onRequestMinimize: (() -> Unit)? = {
        state.isMinimized = true
    },
    onRequestToggleMaximize: (() -> Unit)? = {
        if (state.placement == WindowPlacement.Maximized) {
            state.placement = WindowPlacement.Floating
        } else {
            state.placement = WindowPlacement.Maximized
        }
    },
    windowController: WindowController = remember {
        WindowController()
    },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    alwaysOnTop: Boolean = false,
    preventMinimize: Boolean = onRequestMinimize == null,
    content: @Composable FrameWindowScope.() -> Unit,
) {
    val start = windowController.start
    val end = windowController.end
    val title = windowController.title.orEmpty()
    val titlePosition = windowController.titlePosition
    val icon = windowController.icon ?: defaultAppIcon.rememberPainter()


    val transparent: Boolean
    val undecorated: Boolean
    val isAeroSnapSupported = CustomWindowDecorationAccessing.isSupported
    if (isAeroSnapSupported) {
        //we use aero snap
        transparent = false
        undecorated = false
    } else {
        //we decorate window and add our custom layout
        transparent = true
        undecorated = true
    }
    Window(
        state = state,
        transparent = transparent,
        undecorated = undecorated,
        icon = icon,
        title = title,
        resizable = resizable,
        onCloseRequest = onCloseRequest,
        onKeyEvent = onKeyEvent,
        alwaysOnTop = alwaysOnTop,
    ) {
        val isLight = myColors.isLight
        val background = myColors.background
        LaunchedEffect(background) {
            withContext(Dispatchers.Main) {
                //I set window background fix window edge flickering on window resize
                window.background = background.takeOrElse {
                    if (isLight) Color.White
                    else Color.Black
                }.toWindowColorType()
            }
        }
        UiScaledContent {
            CompositionLocalProvider(
                LocalWindowController provides windowController,
                LocalWindowState provides state,
                LocalWindow provides window,
            ) {
                if (preventMinimize) {
                    PreventMinimize()
                }
                // a window frame which totally rendered with compose
                CustomWindowFrame(
                    onRequestMinimize = onRequestMinimize,
                    onRequestClose = onCloseRequest,
                    onRequestToggleMaximize = onRequestToggleMaximize,
                    title = title,
                    titlePosition = titlePosition,
                    windowIcon = icon,
                    background = background,
                    onBackground = myColors.onBackground,
                    start = start,
                    end = end,
                ) {
                    ResponsiveBox {
                        Box(Modifier.clearFocusOnTap()) {
                            PopUpContainer {
                                content()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreventMinimize() {
    val state = LocalWindowState.current
    LaunchedEffect(state.isMinimized) {
        if (state.isMinimized) {
            state.isMinimized = false
        }
    }
}

private fun Modifier.clearFocusOnTap(): Modifier = composed {
//    for now we don't change it
    Modifier
//    val focusManager = LocalFocusManager.current
//    Modifier.pointerInput(Unit) {
//        awaitEachGesture {
//            awaitFirstDown(pass = PointerEventPass.Main)
//            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Main)
//            if (upEvent != null) {
//                focusManager.clearFocus()
//            }
//        }
//    }
}

class WindowController(
    title: String? = null,
    icon: Painter? = null,
) {
    var title by mutableStateOf(title)
    var titlePosition by mutableStateOf(TitlePosition.default())
    var icon by mutableStateOf(icon)
    var start: (@Composable () -> Unit)? by mutableStateOf(null)
    var end: (@Composable () -> Unit)? by mutableStateOf(null)
}

@Immutable
data class TitlePosition(
    val centered: Boolean = false,
    val afterStart: Boolean = false,
    val padding: PaddingValues = PaddingValues(0.dp),
) {
    companion object {
        fun default() = TitlePosition()
    }
}

@Composable
fun rememberWindowController(
    title: String? = null,
    icon: Painter? = null,
): WindowController {
    val controller = remember {
        WindowController(
            title, icon
        )
    }
    LaunchedEffect(title) {
        controller.title = title
    }
    LaunchedEffect(icon) {
        controller.icon = icon
    }
    return controller
}

private val LocalWindowController = compositionLocalOf<WindowController> { error("window controller not provided") }
private val LocalWindowState = compositionLocalOf<WindowState> { error("window controller not provided") }

@Composable
fun WindowStart(content: @Composable () -> Unit) {
    val c = LocalWindowController.current
    c.start = content
    DisposableEffect(Unit) {
        onDispose {
            c.start = null
        }
    }
}

@Composable
fun WindowEnd(content: @Composable () -> Unit) {
    val c = LocalWindowController.current
    c.end = content
    DisposableEffect(Unit) {
        onDispose {
            c.end = null
        }
    }
}

@Composable
fun WindowTitle(title: String) {
    val c = LocalWindowController.current
    LaunchedEffect(title) {
        c.title = title
    }
    DisposableEffect(Unit) {
        onDispose {
            c.title = null
        }
    }
}

@Composable
fun WindowTitlePosition(titlePosition: TitlePosition) {
    val c = LocalWindowController.current
    LaunchedEffect(titlePosition) {
        c.titlePosition = titlePosition
    }
    DisposableEffect(Unit) {
        onDispose {
            c.titlePosition = TitlePosition.default()
        }
    }
}

@Composable
fun WindowIcon(icon: IconSource) {
    WindowIcon(icon.rememberPainter())
}

@Composable
fun WindowIcon(icon: Painter) {
    val current = LocalWindowController.current
    DisposableEffect(icon) {
        current.let {
            it.icon = icon
        }
        onDispose {
            current.let {
                it.icon = null
            }
        }
    }
}
