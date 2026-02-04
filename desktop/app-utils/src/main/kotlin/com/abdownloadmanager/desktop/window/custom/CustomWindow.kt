package com.abdownloadmanager.desktop.window.custom

import com.abdownloadmanager.shared.util.ui.WithContentColor
import ir.amirab.util.compose.IconSource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.abdownloadmanager.shared.util.PopUpContainer
import com.abdownloadmanager.shared.util.ResponsiveBox
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.WithTitleBarDirection
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.UiScaledContent
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.Text
import ir.amirab.util.desktop.LocalFrameWindowScope
import com.abdownloadmanager.shared.ui.util.LocalWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.ui.icon.LocalNewUiChecker
import org.jetbrains.jewel.ui.icon.NewUiChecker
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle


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

    val isDark = !myColors.isLight
    val background = myColors.background
    val onBackground = myColors.onBackground

    // Create Jewel decoration styles using ABDM's colors for the title bar
    val decoratedWindowStyle = if (isDark) DecoratedWindowStyle.dark() else DecoratedWindowStyle.light()
    val titleBarStyle = if (isDark) {
        TitleBarStyle.dark(
            colors = TitleBarColors.dark(
                backgroundColor = background,
                inactiveBackground = background,
                contentColor = onBackground,
            ),
        )
    } else {
        TitleBarStyle.light(
            colors = TitleBarColors.light(
                backgroundColor = background,
                inactiveBackground = background,
                contentColor = onBackground,
            ),
        )
    }

    DecoratedWindow(
        state = state,
        onCloseRequest = onCloseRequest,
        title = title,
        icon = icon,
        resizable = resizable,
        alwaysOnTop = alwaysOnTop,
        onKeyEvent = onKeyEvent,
        style = decoratedWindowStyle,
    ) {
        val decoratedScope = this

        LaunchedEffect(background) {
            withContext(Dispatchers.Main) {
                window.background = background.takeOrElse {
                    if (!isDark) Color.White
                    else Color.Black
                }.toWindowColorType()
            }
        }

        // Provide Jewel composition locals required by TitleBar components (e.g. Icon on Linux)
        CompositionLocalProvider(
            LocalNewUiChecker provides NewUiChecker { true },
        ) {
        UiScaledContent {
            CompositionLocalProvider(
                LocalWindowController provides windowController,
                LocalWindowState provides state,
                LocalWindow provides window,
                LocalFrameWindowScope provides decoratedScope
            ) {
                if (preventMinimize) {
                    PreventMinimize()
                }
                WithTitleBarDirection {
                    decoratedScope.TitleBar(style = titleBarStyle) { _ ->
                        // Icon
                        Row(
                            Modifier.align(Alignment.Start),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(16.dp))
                            WithContentAlpha(1f) {
                                Image(icon, null, Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        // Title before start content (default position)
                        if (!titlePosition.afterStart) {
                            TitleBarTitle(
                                title = title,
                                modifier = Modifier
                                    .align(
                                        if (titlePosition.centered) Alignment.CenterHorizontally
                                        else Alignment.Start
                                    )
                                    .padding(titlePosition.padding),
                            )
                        }

                        // Start content
                        start?.let {
                            Row(Modifier.align(Alignment.Start)) {
                                it()
                                Spacer(Modifier.width(8.dp))
                            }
                        }

                        // Title after start content
                        if (titlePosition.afterStart) {
                            TitleBarTitle(
                                title = title,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(titlePosition.padding),
                            )
                        }

                        // End content
                        end?.let {
                            Row(Modifier.align(Alignment.End)) {
                                it()
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                }

                WithContentColor(onBackground) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(background)
                    ) {
                        ResponsiveBox {
                            Box(Modifier.clearFocusOnTap()) {
                                PopUpContainer {
                                    decoratedScope.content()
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun TitleBarTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    val isWindowFocused = isWindowFocused()
    WithContentColor(myColors.onBackground) {
        WithContentAlpha(
            animateFloatAsState(
                if (isWindowFocused) 1f else 0.5f
            ).value
        ) {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = myTextSizes.base,
                modifier = modifier,
            )
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
    val focusManager = LocalFocusManager.current
    Modifier.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(pass = PointerEventPass.Main)
            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Main)
            if (upEvent != null) {
                focusManager.clearFocus()
            }
        }
    }
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
