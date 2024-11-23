package com.abdownloadmanager.desktop.ui.customwindow

import com.abdownloadmanager.utils.compose.LocalContentColor
import com.abdownloadmanager.desktop.ui.theme.myColors
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.WindowClose
import com.abdownloadmanager.desktop.ui.icons.default.WindowFloating
import com.abdownloadmanager.desktop.ui.icons.default.WindowMaximize
import com.abdownloadmanager.desktop.ui.icons.default.WindowMinimize
import com.abdownloadmanager.utils.compose.widget.Icon
import ir.amirab.util.customwindow.HitSpots
import ir.amirab.util.customwindow.windowFrameItem

@Composable
fun SystemButton(
    onClick: () -> Unit,
    background: Color = Color.Transparent,
    onBackground: Color = LocalContentColor.current,
    hoveredBackgroundColor: Color = background,
    onHoveredBackgroundColor: Color = LocalContentColor.current,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val isFocused = isWindowFocused()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = animateColorAsState(
            when {
                isHovered -> onHoveredBackgroundColor
                else -> onBackground
            }.copy(
                alpha = if (isFocused || isHovered) {
                    1f
                } else {
                    0.25f
                }
            )
        ).value,
        modifier = modifier
            .clickable { onClick() }
            .background(
                animateColorAsState(
                    when {
                        isHovered -> hoveredBackgroundColor
                        else -> background
                    }
                ).value
            )
            .hoverable(interactionSource)
            .windowButton()
    )
}


@Composable
fun CloseButton(
    onRequestClose: () -> Unit,
    modifier: Modifier,
) {
    SystemButton(
        onRequestClose,
        background = Color.Transparent,
        onBackground = myColors.onBackground,
        hoveredBackgroundColor = Color(0xFFc42b1c),
        onHoveredBackgroundColor = myColors.onError,
        icon = AbIcons.Default.WindowClose,
        modifier = modifier,
    )
}

private fun Modifier.windowButton(): Modifier {
    return fillMaxHeight()
        .wrapContentHeight()
        .padding(
            horizontal = 20.dp,
        )
        .requiredSize(8.dp)
}

@Composable
fun FrameWindowScope.WindowsActionButtons(
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onToggleMaximize: (() -> Unit)?,
) {
    Row(
        // Toolbar is aligned center vertically, so I fill that and place it on top
        modifier = Modifier.fillMaxHeight().wrapContentHeight(Alignment.Top),
        verticalAlignment = Alignment.Top
    ) {
        onRequestMinimize?.let {
            SystemButton(
                icon = AbIcons.Default.WindowMinimize,
                onClick = onRequestMinimize,
                modifier = Modifier.windowFrameItem("minimize", HitSpots.MINIMIZE_BUTTON)
            )
        }

        onToggleMaximize?.let {
            SystemButton(
                icon =
                if (isWindowMaximized()) {
                    AbIcons.Default.WindowFloating
                } else {
                    AbIcons.Default.WindowMaximize
                },
                onClick = onToggleMaximize,
                modifier = Modifier.windowFrameItem("maximize", HitSpots.MAXIMIZE_BUTTON)
            )
        }

        CloseButton(
            onRequestClose = onRequestClose,
            modifier = Modifier.windowFrameItem("close", HitSpots.CLOSE_BUTTON)
        )
    }
}