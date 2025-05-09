package com.abdownloadmanager.desktop.window.custom

import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons

@Composable
private fun SystemButton(
    onClick: () -> Unit,
    background: Color = LocalContentColor.current / 0.1f,
    onBackground: Color = LocalContentColor.current,
    hoveredBackgroundColor: Color = LocalContentColor.current / 0.2f,
    onHoveredBackgroundColor: Color = LocalContentColor.current,
    icon: IconSource,
    modifier: Modifier = Modifier,
) {
    val isFocused = isWindowFocused()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    MyIcon(
        icon = icon,
        contentDescription = null,
        tint = animateColorAsState(
            when {
                isHovered -> onHoveredBackgroundColor
                else -> onBackground
            }.copy(
                alpha = if (isFocused || isHovered) {
                    1f
                } else {
                    0.5f
                }
            )
        ).value,
        modifier = modifier
            .hoverable(interactionSource)
            .onClick { onClick() }
            .fillMaxHeight()
            .wrapContentHeight()
            .padding(horizontal = 4.dp)
            .background(
                animateColorAsState(
                    when {
                        isHovered -> hoveredBackgroundColor
                        else -> background
                    }
                ).value,
                CircleShape
            )
            .padding(6.dp)
            .requiredSize(6.dp)
    )
}


@Composable
fun FrameWindowScope.LinuxSystemButtons(
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onToggleMaximize: (() -> Unit)?,
) {
    Row(
        // Toolbar is aligned center vertically, so I fill that and place it on top
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .fillMaxHeight().wrapContentHeight(Alignment.Top),
        verticalAlignment = Alignment.Top
    ) {
        onRequestMinimize?.let {
            WindowMinimizeTooltip {
                SystemButton(
                    icon = MyIcons.windowMinimize,
                    onClick = onRequestMinimize,
                    modifier = Modifier
                )
            }
        }

        onToggleMaximize?.let {
            WindowToggleMaximizeTooltip {
                SystemButton(
                    icon = if (isWindowMaximized()) {
                        MyIcons.windowFloating
                    } else {
                        MyIcons.windowMaximize
                    },
                    onClick = onToggleMaximize,
                    modifier = Modifier
                )
            }
        }

        WindowCloseButtonTooltip {
            SystemButton(
                onRequestClose,
                icon = MyIcons.windowClose,
                modifier = Modifier,
            )
        }
    }
}
