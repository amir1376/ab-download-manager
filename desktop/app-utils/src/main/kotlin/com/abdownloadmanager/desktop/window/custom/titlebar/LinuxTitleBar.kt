package com.abdownloadmanager.desktop.window.custom.titlebar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.abdownloadmanager.desktop.window.custom.titlebar.LinuxSystemButtons
import com.abdownloadmanager.desktop.window.custom.TitlePosition

object LinuxTitleBar : TitleBar {
    override val systemButtonsFirst: Boolean = false
    override val titleBarHeight: Dp = TitleBar.DefaultTitleBarHeigh

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


}
