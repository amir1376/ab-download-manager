package com.abdownloadmanager.desktop.window.custom.titlebar

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
import com.abdownloadmanager.shared.utils.darker
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource
import ir.amirab.util.ifThen

@Composable
internal fun MacOSSystemButtons(
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onToggleMaximize: (() -> Unit)?,
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
        CloseButton(onRequestClose, isUserInThisArea)
        MinimizeButton(onRequestMinimize, isUserInThisArea)
        ToggleMaximizeButton(onToggleMaximize, isUserInThisArea)
    }
}

@Composable
private fun MinimizeButton(onRequestMinimize: (() -> Unit)?, isUserInThisArea: Boolean) {
    onRequestMinimize?.let {
        _root_ide_package_.com.abdownloadmanager.desktop.window.custom.WindowMinimizeTooltip {
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
        _root_ide_package_.com.abdownloadmanager.desktop.window.custom.WindowToggleMaximizeTooltip {
            SystemButton(
                onClick = onToggleMaximize,
                modifier = Modifier,
                hoveredBackgroundColor = Color(0xFF28C840),
                icon = if (_root_ide_package_.com.abdownloadmanager.desktop.window.custom.isWindowMaximized()) {
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
    _root_ide_package_.com.abdownloadmanager.desktop.window.custom.WindowCloseButtonTooltip {
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
    val isWindowFocused = _root_ide_package_.com.abdownloadmanager.desktop.window.custom.isWindowFocused()
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
