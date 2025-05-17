package com.abdownloadmanager.desktop.window.custom.titlebar.linux

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.abdownloadmanager.desktop.window.custom.TitlePosition
import com.abdownloadmanager.desktop.window.custom.isWindowFocused
import com.abdownloadmanager.desktop.window.custom.titlebar.CommonRenderTitleBar
import com.abdownloadmanager.desktop.window.custom.titlebar.CommonTitleBarContent
import com.abdownloadmanager.desktop.window.custom.titlebar.SystemButtonType
import com.abdownloadmanager.desktop.window.custom.titlebar.SystemButtonsPosition
import com.abdownloadmanager.desktop.window.custom.titlebar.TitleBar
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.shared.utils.ui.myColors

object LinuxTitleBar : TitleBar {
    override val titleBarHeight: Dp = TitleBar.Companion.DefaultTitleBarHeigh
    override val systemButtonsPosition: SystemButtonsPosition by lazy {
        LinuxSystemButtonsProvider.getPositions()
            ?: SystemButtonsPosition(
                buttons = listOf(
                    SystemButtonType.Minimize,
                    SystemButtonType.Maximize,
                    SystemButtonType.Close,
                ),
                isLeft = false,
            )
    }

    @Composable
    override fun RenderSystemButtons(
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onToggleMaximize: (() -> Unit)?
    ) {
        LinuxSystemButtons(
            onRequestClose = onRequestClose,
            onRequestMinimize = onRequestMinimize,
            onToggleMaximize = onToggleMaximize,
            buttons = systemButtonsPosition.buttons,
        )
    }

    @Composable
    override fun RenderTitleBarContent(
        title: String,
        titlePosition: TitlePosition,
        modifier: Modifier,
        windowIcon: Painter?,
        start: @Composable (() -> Unit)?,
        end: @Composable (() -> Unit)?
    ) {
        CommonTitleBarContent(
            title = title,
            windowIcon = windowIcon,
            titlePosition = titlePosition,
            start = start,
            end = end,
            modifier = modifier,
        )
    }

    @Composable
    override fun RenderTitleBar(
        modifier: Modifier,
        titleBar: TitleBar,
        title: String,
        windowIcon: Painter?,
        titlePosition: TitlePosition,
        start: @Composable (() -> Unit)?,
        end: @Composable (() -> Unit)?,
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onRequestToggleMaximize: (() -> Unit)?
    ) {
        val windowFocused = isWindowFocused()
        CommonRenderTitleBar(
            modifier = modifier
                .background(
                    animateColorAsState(
                        if (windowFocused) Color.Companion.Transparent
                        else myColors.onBackground / 0.05f
                    ).value
                ),
            titleBar = titleBar,
            title = title,
            windowIcon = windowIcon,
            titlePosition = titlePosition,
            start = start,
            end = end,
            onRequestClose = onRequestClose,
            onRequestMinimize = onRequestMinimize,
            onRequestToggleMaximize = onRequestToggleMaximize,
        )
    }

}
