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
import androidx.compose.ui.unit.dp
import com.jetbrains.JBR
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.window.styling.TitleBarStyle
import org.jetbrains.jewel.window.utils.DialogCloseButton
import java.awt.event.MouseEvent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DecoratedDialogScope.DialogTitleBarOnLinux(
    modifier: Modifier = Modifier,
    gradientStartColor: Color = Color.Unspecified,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    content: @Composable TitleBarScope.(DecoratedDialogState) -> Unit,
) {
    val linuxStyle = createLinuxTitleBarStyle(style)
    val dialogState = state

    DialogTitleBarImpl(
        modifier.onPointerEvent(PointerEventType.Press, PointerEventPass.Main) {
            if (
                this.currentEvent.button == PointerButton.Primary &&
                this.currentEvent.changes.any { changed -> !changed.isConsumed }
            ) {
                JBR.getWindowMove()?.startMovingTogetherWithMouse(window, MouseEvent.BUTTON1)
            }
        },
        gradientStartColor,
        linuxStyle,
        { _, _ -> PaddingValues(0.dp) },
    ) { _ ->
        // Dialog only shows close button (no minimize/maximize)
        DialogCloseButton(window, dialogState, linuxStyle, iconHoveredEffect = true)
        content(dialogState)
    }
}
