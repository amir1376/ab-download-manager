package com.abdownloadmanager.desktop.window.custom.titlebar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.window.custom.MacOSSystemButtons
import com.abdownloadmanager.desktop.window.custom.TitlePosition
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import ir.amirab.util.ifThen

object MacTitleBar : TitleBar {
    override val systemButtonsFirst: Boolean = true
    override val titleBarHeight: Dp = TitleBar.DefaultTitleBarHeigh

    @Composable
    override fun RenderSystemButtons(
        onRequestClose: () -> Unit,
        onRequestMinimize: (() -> Unit)?,
        onToggleMaximize: (() -> Unit)?
    ) {
        MacOSSystemButtons(
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
        MacTitleBarContent(
            title = title,
            windowIcon = windowIcon,
            titlePosition = titlePosition,
            start = start,
            end = end,
            modifier = modifier,
        )
    }
}

@Composable
private fun MacTitleBarContent(
    modifier: Modifier,
    title: String,
    windowIcon: Painter?,
    titlePosition: TitlePosition,
    start: @Composable (() -> Unit)?,
    end: @Composable (() -> Unit)?
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!titlePosition.afterStart) {
            Row(
                Modifier
                    .ifThen(titlePosition.centered) {
                        weight(1f)
                            .ifThen(start == null) {
                                wrapContentWidth()
                            }
                    }
                    .padding(titlePosition.padding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(8.dp))
                windowIcon?.let {
                    WithContentAlpha(1f) {
                        Image(it, null, Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Title(
                    modifier = Modifier,
                    title = title
                )
            }
        }
        start?.let {
            Row(
                Modifier
            ) {
                start()
                Spacer(Modifier.width(8.dp))
            }
        }
        if (titlePosition.afterStart) {
            Row(
                Modifier
                    .weight(1f)
                    .ifThen(titlePosition.centered) {
                        wrapContentWidth()
                    }
                    .padding(titlePosition.padding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(8.dp))
                windowIcon?.let {
                    WithContentAlpha(1f) {
                        Image(it, null, Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Title(
                    modifier = Modifier,
                    title = title
                )
            }
        }
        if (!titlePosition.centered && !titlePosition.afterStart) {
            Spacer(Modifier.weight(1f))
        }
        end?.let {
            Row(
                Modifier
            ) {
                end()
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}
