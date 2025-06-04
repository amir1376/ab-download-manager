package com.abdownloadmanager.desktop.window.custom

import com.abdownloadmanager.shared.utils.ui.WithContentColor
import ir.amirab.util.compose.IconSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.abdownloadmanager.desktop.window.custom.titlebar.TitleBar
import com.abdownloadmanager.shared.utils.PopUpContainer
import com.abdownloadmanager.shared.utils.ResponsiveBox
import com.abdownloadmanager.shared.utils.ui.WithTitleBarDirection
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.LocalUiScale
import com.abdownloadmanager.shared.utils.ui.theme.UiScaledContent
import com.jetbrains.JBR
import com.jetbrains.WindowDecorations
import com.jetbrains.WindowMove
import ir.amirab.util.desktop.LocalFrameWindowScope
import ir.amirab.util.desktop.LocalWindow
import ir.amirab.util.desktop.screen.applyUiScale
import ir.amirab.util.ifThen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent


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
                .ifThen(!JBR.isWindowDecorationsSupported()) {
                    ifThen(isWindowFloating()) {
                        border(1.dp, Color.Gray.copy(0.25f), RectangleShape)
                            .padding(1.dp)
                    }
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
    val titleBar = TitleBar.getPlatformTitleBar()
    if (JBR.isWindowDecorationsSupported()) {
        val density = LocalDensity.current
        val uiScale = LocalUiScale.current
        fun computeHeaderHeight(height: Dp): Float {
            return height.value.applyUiScale(uiScale)
        }

        var headerHeight by remember {
            mutableStateOf(computeHeaderHeight(titleBar.titleBarHeight))
        }
        val customTitleBar = remember {
            JBR.getWindowDecorations().createCustomTitleBar()
        }
        LaunchedEffect(headerHeight) {
            customTitleBar.height = headerHeight
            customTitleBar.putProperty("controls.visible", false)
            JBR.getWindowDecorations().setCustomTitleBar(window, customTitleBar)
        }
        Box(
            Modifier
                .onSizeChanged {
                    headerHeight = computeHeaderHeight(
                        density.run { it.height.toDp() }
                    )
                }
        ) {
            Spacer(
                Modifier
                    .matchParentSize()
                    .customTitleBarMouseEventHandler(customTitleBar)
            )
            FrameContent(
                titleBar = titleBar,
                modifier = Modifier,
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
    } else {
        SystemDraggableSection(
            onRequestToggleMaximize = onRequestToggleMaximize,
        ) { modifier ->
            FrameContent(
                titleBar = titleBar,
                modifier = modifier,
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

@Composable
internal fun FrameWindowScope.SystemDraggableSection(
    onRequestToggleMaximize: (() -> Unit)?,
    content: @Composable (Modifier) -> Unit
) {
    val windowMove: WindowMove? = JBR.getWindowMove()
    val viewConfig = LocalViewConfiguration.current
    var lastPress by remember { mutableStateOf(0L) }
    if (windowMove != null) {
        content(
            Modifier
                .onPointerEvent(PointerEventType.Press, PointerEventPass.Main) {
                    if (
                        this.currentEvent.button == PointerButton.Primary &&
                        this.currentEvent.changes.any { changed -> !changed.isConsumed }
                    ) {
                        windowMove.startMovingTogetherWithMouse(window, MouseEvent.BUTTON1)
                        if (
                            System.currentTimeMillis() - lastPress in
                            viewConfig.doubleTapMinTimeMillis..viewConfig.doubleTapTimeoutMillis
                        ) {
                            onRequestToggleMaximize?.invoke()
                        }
                        lastPress = System.currentTimeMillis()
                    }
                },
        )
    } else {
        WindowDraggableArea {
            content(Modifier)
        }
    }
}

internal fun Modifier.customTitleBarMouseEventHandler(
    titleBar: WindowDecorations.CustomTitleBar
): Modifier =
    pointerInput(Unit) {
        val currentContext = currentCoroutineContext()
        awaitPointerEventScope {
            var inUserControl = false
            while (currentContext.isActive) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                event.changes.forEach {
                    if (!it.isConsumed && !inUserControl) {
                        titleBar.forceHitTest(false)
                    } else {
                        if (event.type == PointerEventType.Press) {
                            inUserControl = true
                        }
                        if (event.type == PointerEventType.Release) {
                            inUserControl = false
                        }
                        titleBar.forceHitTest(true)
                    }
                }
            }
        }
    }

@Composable
private fun FrameWindowScope.getCurrentWindowSize(): DpSize {
    var windowSize by remember {
        mutableStateOf(DpSize(window.width.dp, window.height.dp))
    }
    //observe window size
    DisposableEffect(window) {
        val listener = object : ComponentAdapter() {
            override fun componentResized(p0: ComponentEvent?) {
                windowSize = DpSize(window.width.dp, window.height.dp)
            }
        }
        window.addComponentListener(listener)
        onDispose {
            window.removeComponentListener(listener)
        }
    }
    return windowSize
}


@Composable
private fun FrameContent(
    titleBar: TitleBar,
    modifier: Modifier,
    title: String,
    windowIcon: Painter? = null,
    titlePosition: TitlePosition,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
) {
    titleBar.RenderTitleBar(
        titleBar = titleBar,
        modifier = modifier
            .fillMaxWidth(),
        title = title,
        windowIcon = windowIcon,
        titlePosition = titlePosition,
        start = start,
        end = end,
        onRequestMinimize = onRequestMinimize,
        onRequestToggleMaximize = onRequestToggleMaximize,
        onRequestClose = onRequestClose,
    )
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
    val isAeroSnapSupported = JBR.isWindowDecorationsSupported()
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
                LocalFrameWindowScope provides this
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

private val LocalWindowController =
    compositionLocalOf<WindowController> { error("window controller not provided") }
private val LocalWindowState =
    compositionLocalOf<WindowState> { error("window controller not provided") }

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
