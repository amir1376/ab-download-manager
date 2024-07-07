package com.abdownloadmanager.desktop.ui.customwindow

import ir.amirab.util.customwindow.ProvideWindowSpotContainer
import com.abdownloadmanager.desktop.ui.WithContentAlpha
import com.abdownloadmanager.desktop.ui.WithContentColor
import com.abdownloadmanager.desktop.ui.icon.IconSource
import com.abdownloadmanager.desktop.ui.icon.MyIcons
//import com.abdownloadmanager.desktop.ui.theme.LocalUiScale
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.utils.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
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
import ir.amirab.util.desktop.LocalWindow
import ir.amirab.util.customwindow.HitSpots
import ir.amirab.util.customwindow.util.CustomWindowDecorationAccessing
import ir.amirab.util.customwindow.windowFrameItem


// a window frame which totally rendered with compose
@Composable
private fun FrameWindowScope.CustomWindowFrame(
    onRequestMinimize: (() -> Unit)?,
    onRequestClose: () -> Unit,
    onRequestToggleMaximize: (() -> Unit)?,
    title: String,
    windowIcon: Painter? = null,
    background: Color,
    onBackground: Color,
    center: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
//    val borderColor = MaterialTheme.colors.surface
    WithContentColor(onBackground) {
        Column(
            Modifier
                .fillMaxSize()
                .background(background)
        ) {
            SnapDraggableToolbar(
                title = title,
                windowIcon = windowIcon,
                center = center,
                onRequestMinimize = onRequestMinimize,
                onRequestClose = onRequestClose,
                onRequestToggleMaximize = onRequestToggleMaximize
            )
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
    center: @Composable () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
) {
    ProvideWindowSpotContainer {
        if (CustomWindowDecorationAccessing.isSupported) {
            FrameContent(title, windowIcon, center, onRequestMinimize, onRequestToggleMaximize, onRequestClose)
        } else {
            WindowDraggableArea {
                FrameContent(title, windowIcon, center, onRequestMinimize, onRequestToggleMaximize, onRequestClose)
            }
        }
    }
}

@Composable
private fun FrameWindowScope.FrameContent(
    title: String,
    windowIcon: Painter? = null,
    center: @Composable () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .fillMaxHeight()
                    .windowFrameItem("icon", HitSpots.MENU_BAR),
                verticalAlignment = Alignment.CenterVertically,
            ){
                Spacer(Modifier.width(16.dp))
                windowIcon?.let {
                    WithContentAlpha(1f){
                        Image(it, null, Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }
            WithContentColor(myColors.onBackground) {
                WithContentAlpha(1f) {
                    Text(
                        title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = myTextSizes.base,
                        modifier = Modifier
                            .windowFrameItem("title", HitSpots.DRAGGABLE_AREA)
                    )
                }
            }
            Box(Modifier.weight(1f)) {
                center()
            }
        }
        WindowsActionButtons(
            onRequestClose,
            onRequestMinimize,
            onRequestToggleMaximize,
        )
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
    val center = windowController.center ?: {}
    val title = windowController.title.orEmpty()
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
            //I set window background fix window edge flickering on window resize
            window.background = background.takeOrElse {
                if (isLight) Color.White
                else Color.Black
            }.toWindowColorType()
        }
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
                windowIcon = icon,
                background = background,
                onBackground = myColors.onBackground,
                center = { center() }
            ) {
//                val defaultDensity = LocalDensity.current
//                val uiScale = LocalUiScale.current
//                val density = remember(uiScale) {
//                    if (uiScale == null) {
//                        defaultDensity
//                    } else {
//                        Density(uiScale)
//                    }
//                }
//                CompositionLocalProvider(
//                    LocalDensity provides density
//                ) {
                ResponsiveBox {
                    Box(Modifier.clearFocusOnTap()) {
                        PopUpContainer {
                            content()
                        }
                    }
                }
//                }
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
    var icon by mutableStateOf(icon)
    var center: (@Composable () -> Unit)? by mutableStateOf(null)
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
fun WindowCenter(content: @Composable () -> Unit) {
    val c = LocalWindowController.current
    c.center = content
    DisposableEffect(Unit) {
        onDispose {
            c.center = null
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