package org.jetbrains.jewel.window

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import com.jetbrains.JBR
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.styling.IconButtonColors
import org.jetbrains.jewel.ui.component.styling.IconButtonStyle
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle
import org.jetbrains.jewel.window.utils.WindowControlArea
import org.jetbrains.jewel.window.utils.linux.LinuxTitleBarIconsFactory
import java.awt.Frame
import java.awt.event.MouseEvent

/**
 * Creates a Linux-specific title bar style with appropriate icons and transparent button backgrounds.
 * This is shared between TitleBar and DialogTitleBar on Linux.
 */
@Composable
internal fun createLinuxTitleBarStyle(style: TitleBarStyle): TitleBarStyle {
    val linuxIconButtonStyle =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    foregroundSelectedActivated = Color.Transparent,
                    background = Color.Transparent,
                    backgroundDisabled = Color.Transparent,
                    backgroundSelected = Color.Transparent,
                    backgroundSelectedActivated = Color.Transparent,
                    backgroundFocused = Color.Transparent,
                    backgroundPressed = Color.Transparent,
                    backgroundHovered = Color.Transparent,
                    border = Color.Transparent,
                    borderDisabled = Color.Transparent,
                    borderSelected = Color.Transparent,
                    borderSelectedActivated = Color.Transparent,
                    borderFocused = Color.Transparent,
                    borderPressed = Color.Transparent,
                    borderHovered = Color.Transparent,
                ),
            metrics = style.iconButtonStyle.metrics,
        )

    val linuxIcons = LinuxTitleBarIconsFactory.createForCurrentDesktop()
    return TitleBarStyle(
        colors =
            TitleBarColors(
                background = style.colors.background,
                inactiveBackground = style.colors.inactiveBackground,
                content = style.colors.content,
                border = style.colors.border,
                fullscreenControlButtonsBackground = style.colors.fullscreenControlButtonsBackground,
                titlePaneButtonHoveredBackground = style.colors.titlePaneButtonHoveredBackground,
                titlePaneButtonPressedBackground = style.colors.titlePaneButtonPressedBackground,
                titlePaneCloseButtonHoveredBackground = Color.Transparent,
                titlePaneCloseButtonPressedBackground = Color.Transparent,
                iconButtonHoveredBackground = style.colors.iconButtonHoveredBackground,
                iconButtonPressedBackground = style.colors.iconButtonPressedBackground,
                dropdownPressedBackground = style.colors.dropdownPressedBackground,
                dropdownHoveredBackground = style.colors.dropdownHoveredBackground,
            ),
        metrics = style.metrics,
        icons = linuxIcons,
        dropdownStyle = style.dropdownStyle,
        iconButtonStyle = style.iconButtonStyle,
        paneButtonStyle = linuxIconButtonStyle,
        paneCloseButtonStyle = linuxIconButtonStyle,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DecoratedWindowScope.TitleBarOnLinux(
    modifier: Modifier = Modifier,
    gradientStartColor: Color = Color.Unspecified,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    content: @Composable TitleBarScope.(DecoratedWindowState) -> Unit,
) {
    val linuxStyle = createLinuxTitleBarStyle(style)

    var lastPress = 0L
    val viewConfig = LocalViewConfiguration.current
    TitleBarImpl(
        modifier.onPointerEvent(PointerEventType.Press, PointerEventPass.Main) {
            if (
                this.currentEvent.button == PointerButton.Primary &&
                this.currentEvent.changes.any { changed -> !changed.isConsumed }
            ) {
                JBR.getWindowMove()?.startMovingTogetherWithMouse(window, MouseEvent.BUTTON1)
                if (
                    System.currentTimeMillis() - lastPress in
                    viewConfig.doubleTapMinTimeMillis..viewConfig.doubleTapTimeoutMillis
                ) {
                    if (state.isMaximized) {
                        window.extendedState = Frame.NORMAL
                    } else {
                        window.extendedState = Frame.MAXIMIZED_BOTH
                    }
                }
                lastPress = System.currentTimeMillis()
            }
        },
        gradientStartColor,
        linuxStyle,
        { _, _ -> PaddingValues(0.dp) },
    ) { state ->
        WindowControlArea(window, state, linuxStyle, iconHoveredEffect = true)
        content(state)
    }
}
