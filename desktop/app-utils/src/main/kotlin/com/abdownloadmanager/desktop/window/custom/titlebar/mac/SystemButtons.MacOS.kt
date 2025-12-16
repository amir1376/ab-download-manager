package com.abdownloadmanager.desktop.window.custom.titlebar.mac

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
import com.abdownloadmanager.desktop.window.custom.WindowCloseButtonTooltip
import com.abdownloadmanager.desktop.window.custom.WindowMinimizeTooltip
import com.abdownloadmanager.desktop.window.custom.WindowToggleMaximizeTooltip
import com.abdownloadmanager.desktop.window.custom.isWindowFocused
import com.abdownloadmanager.desktop.window.custom.isWindowMaximized
import com.abdownloadmanager.desktop.window.custom.titlebar.SystemButtonType
import com.abdownloadmanager.shared.util.darker
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource

@Composable
internal fun MacOSSystemButtons(
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onToggleMaximize: (() -> Unit)?,
    buttons: List<SystemButtonType>,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isUserInThisArea by interactionSource.collectIsHoveredAsState()
    Row(
        // Toolbar is aligned center vertically, so I fill that and place it on top
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .hoverable(interactionSource)
            .fillMaxHeight().wrapContentHeight(Alignment.Top),
        verticalAlignment = Alignment.Top
    ) {
        buttons.forEach {
            when (it) {
                SystemButtonType.Close -> {
                    CloseButton(onRequestClose, isUserInThisArea)
                }

                SystemButtonType.Minimize -> {
                    MinimizeButton(onRequestMinimize, isUserInThisArea)
                }

                SystemButtonType.Maximize -> {
                    ToggleMaximizeButton(onToggleMaximize, isUserInThisArea)
                }
            }
        }
    }
}

@Composable
private fun MinimizeButton(onRequestMinimize: (() -> Unit)?, isUserInThisArea: Boolean) {
    onRequestMinimize?.let {
        WindowMinimizeTooltip {
            SystemButton(
                onClick = onRequestMinimize,
                modifier = Modifier,
                hoveredBackgroundColor = Color(0xFFFFBD2E),
                icon = MyIcons.windowMinimize,
                isUserInThisArea = isUserInThisArea,
            )
        }
    }
}

@Composable
private fun ToggleMaximizeButton(onToggleMaximize: (() -> Unit)?, isUserInThisArea: Boolean) {
    onToggleMaximize?.let {
        WindowToggleMaximizeTooltip {
            SystemButton(
                onClick = onToggleMaximize,
                modifier = Modifier,
                hoveredBackgroundColor = Color(0xFF28C840),
                icon = if (isWindowMaximized()) {
                    MyIcons.windowFloating
                } else {
                    MyIcons.windowMaximize
                },
                isUserInThisArea = isUserInThisArea,
            )
        }
    }
}

@Composable
private fun CloseButton(onRequestClose: () -> Unit, isUserInThisArea: Boolean) {
    WindowCloseButtonTooltip {
        SystemButton(
            onRequestClose,
            modifier = Modifier,
            hoveredBackgroundColor = Color(0xFFFF5F57),
            icon = MyIcons.windowClose,
            isUserInThisArea = isUserInThisArea,
        )
    }
}

@Composable
private fun SystemButton(
    onClick: () -> Unit,
    hoveredBackgroundColor: Color,
    unfocusedBackgroundColor: Color = myColors.onBackground / 0.2f,
    icon: IconSource,
    isUserInThisArea: Boolean,
    modifier: Modifier = Modifier,
) {
    val isWindowFocused = isWindowFocused()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = modifier
            .hoverable(interactionSource)
            .onClick { onClick() }
            .fillMaxHeight()
            .wrapContentHeight()
            .padding(horizontal = 6.dp)
            .background(
                animateColorAsState(
                    when {
                        !isWindowFocused -> unfocusedBackgroundColor
                        isHovered -> hoveredBackgroundColor.darker()
                        else -> hoveredBackgroundColor
                    }
                ).value,
                CircleShape
            )
            .requiredSize(12.dp)
    ) {
        if (
            isUserInThisArea && isWindowFocused
        ) {
            MyIcon(
                icon = icon,
                tint = Color.Black,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(5.dp),
                contentDescription = null,
            )
        }
    }
}
