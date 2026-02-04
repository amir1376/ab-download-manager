package org.jetbrains.jewel.window.utils

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.styling.IconButtonStyle
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.painter.PainterHint
import org.jetbrains.jewel.ui.painter.PainterProviderScope
import org.jetbrains.jewel.ui.painter.PainterSuffixHint
import org.jetbrains.jewel.window.DecoratedDialogState
import org.jetbrains.jewel.window.DecoratedWindowState
import org.jetbrains.jewel.window.TitleBarScope
import org.jetbrains.jewel.window.defaultTitleBarStyle
import org.jetbrains.jewel.window.styling.TitleBarStyle
import java.awt.Frame
import java.awt.event.WindowEvent

@Composable
private fun TitleBarScope.CloseButton(
    onClick: () -> Unit,
    state: DecoratedWindowState,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    iconHoveredEffect: Boolean,
) {
    ControlButton(onClick, state, style.icons.closeButton, "Close", style, style.paneCloseButtonStyle, iconHoveredEffect)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TitleBarScope.ControlButton(
    onClick: () -> Unit,
    state: DecoratedWindowState,
    iconKey: IconKey,
    description: String,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    iconButtonStyle: IconButtonStyle = style.paneButtonStyle,
    iconHoveredEffect: Boolean,
) {
    IconButton(
        onClick,
        Modifier.align(Alignment.End).focusable(false).size(style.metrics.titlePaneButtonSize),
        style = iconButtonStyle,
    ) {
        var hovered by remember { mutableStateOf(false) }

        val hoverModifier =
            if (iconHoveredEffect && state.isActive) {
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .drawWithContent {
                        drawContent()
                        if (hovered) {
                            // Lighten only the icon by overlaying a subtle white tint
                            drawRect(Color.White.copy(alpha = 0.02f), blendMode = BlendMode.SrcOver)
                        }
                    }.onPointerEvent(androidx.compose.ui.input.pointer.PointerEventType.Enter) { hovered = true }
                    .onPointerEvent(androidx.compose.ui.input.pointer.PointerEventType.Exit) { hovered = false }
            } else {
                Modifier
            }

        Icon(
            iconKey,
            description,
            hint = if (state.isActive) PainterHint else Inactive,
            modifier = hoverModifier,
        )
    }
}

private data object Inactive : PainterSuffixHint() {
    override fun PainterProviderScope.suffix(): String = "Inactive"
}

@Composable
internal fun TitleBarScope.WindowControlArea(
    window: ComposeWindow,
    state: DecoratedWindowState,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    iconHoveredEffect: Boolean = false,
) {
    CloseButton({ window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING)) }, state, style, iconHoveredEffect)

    // Show maximize/restore button only if window is resizable
    if (window.isResizable) {
        if (state.isMaximized) {
            ControlButton({
                window.extendedState = Frame.NORMAL
            }, state, style.icons.restoreButton, "Restore", iconHoveredEffect = iconHoveredEffect, style = style, iconButtonStyle = style.paneButtonStyle)
        } else {
            ControlButton(
                { window.extendedState = Frame.MAXIMIZED_BOTH },
                state,
                style.icons.maximizeButton,
                "Maximize",
                iconHoveredEffect = iconHoveredEffect,
                style = style,
                iconButtonStyle = style.paneButtonStyle,
            )
        }
    }
    ControlButton(
        { window.extendedState = window.extendedState or Frame.ICONIFIED },
        state,
        style.icons.minimizeButton,
        "Minimize",
        iconHoveredEffect = iconHoveredEffect,
        style = style,
        iconButtonStyle = style.paneButtonStyle,
    )
}

/**
 * Close button for dialog title bars.
 * Unlike [WindowControlArea], this only shows the close button (no minimize/maximize).
 * Uses [TitleBarScope] for consistency with the regular title bar.
 */
@Composable
internal fun TitleBarScope.DialogCloseButton(
    window: java.awt.Window,
    state: DecoratedDialogState,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    iconHoveredEffect: Boolean = false,
) {
    // Reuse ControlButton by converting dialog state to window state
    ControlButton(
        onClick = { window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING)) },
        state = state.toDecoratedWindowState(),
        iconKey = style.icons.closeButton,
        description = "Close",
        style = style,
        iconButtonStyle = style.paneCloseButtonStyle,
        iconHoveredEffect = iconHoveredEffect,
    )
}
