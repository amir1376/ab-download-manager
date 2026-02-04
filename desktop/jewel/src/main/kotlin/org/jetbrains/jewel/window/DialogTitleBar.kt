package org.jetbrains.jewel.window

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.window.styling.TitleBarStyle
import org.jetbrains.jewel.window.utils.DesktopPlatform

/**
 * A title bar for [DecoratedDialog] windows.
 *
 * This reuses the same [TitleBarScope] as the regular [TitleBar], but is designed
 * for modal dialog windows (no minimize/maximize buttons on Linux).
 *
 * @param modifier Modifier to apply to the title bar
 * @param gradientStartColor Optional gradient start color for the background
 * @param style The visual style for the title bar
 * @param content The title bar content, receives [TitleBarScope]
 */
@Composable
public fun DecoratedDialogScope.DialogTitleBar(
    modifier: Modifier = Modifier,
    gradientStartColor: Color = Color.Unspecified,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    content: @Composable TitleBarScope.(DecoratedDialogState) -> Unit,
) {
    // Provide title bar info for the scope
    val titleBarInfo = LocalDialogTitleBarInfo.current
    CompositionLocalProvider(
        LocalTitleBarInfo provides TitleBarInfo(titleBarInfo.title, titleBarInfo.icon),
    ) {
        when (DesktopPlatform.Current) {
            DesktopPlatform.Linux -> DialogTitleBarOnLinux(modifier, gradientStartColor, style, content)
            DesktopPlatform.Windows -> DialogTitleBarOnWindows(modifier, gradientStartColor, style, content)
            DesktopPlatform.MacOS -> DialogTitleBarOnMacOs(modifier, gradientStartColor, style, content)
            DesktopPlatform.Unknown -> error("DialogTitleBar is not supported on this platform(${System.getProperty("os.name")})")
        }
    }
}

@Composable
internal fun DecoratedDialogScope.DialogTitleBarImpl(
    modifier: Modifier = Modifier,
    gradientStartColor: Color = Color.Unspecified,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    applyTitleBar: (Dp, DecoratedWindowState) -> PaddingValues,
    backgroundContent: @Composable () -> Unit = {},
    content: @Composable TitleBarScope.(DecoratedDialogState) -> Unit,
) {
    val dialogState = state
    // Delegate to the common TitleBarImpl by wrapping the content
    GenericTitleBarImpl(
        window = window,
        state = dialogState.toDecoratedWindowState(),
        modifier = modifier,
        gradientStartColor = gradientStartColor,
        style = style,
        applyTitleBar = applyTitleBar,
        backgroundContent = backgroundContent,
    ) { windowState ->
        // Call user content with dialog state instead of window state
        content(dialogState)
    }
}
