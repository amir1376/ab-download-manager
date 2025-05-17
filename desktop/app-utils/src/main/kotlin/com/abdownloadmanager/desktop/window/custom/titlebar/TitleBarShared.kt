package com.abdownloadmanager.desktop.window.custom.titlebar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.window.custom.TitlePosition
import com.abdownloadmanager.desktop.window.custom.isWindowFocused
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import com.abdownloadmanager.shared.utils.ui.WithContentColor
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import ir.amirab.util.ifThen

@Composable
internal fun CommonTitleBarContent(
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
        Row(
            Modifier
                .onClick {
//                         capture pointer
                }
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(16.dp))
            windowIcon?.let {
                WithContentAlpha(1f) {
                    Image(it, null, Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
            }
        }
        if (!titlePosition.afterStart) {
            Title(
                modifier = Modifier
                    .ifThen(titlePosition.centered) {
                        weight(1f)
                            .ifThen(start == null) {
                                wrapContentWidth()
                            }
                    }
                    .padding(titlePosition.padding),
                title = title
            )
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
            Title(
                modifier = Modifier
                    .weight(1f)
                    .ifThen(titlePosition.centered) {
                        wrapContentWidth()
                    }
                    .padding(titlePosition.padding),
                title = title
            )
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

@Composable
fun Title(
    modifier: Modifier, title: String,
) {
    val isWindowFocused = isWindowFocused()
    WithContentColor(myColors.onBackground) {
        WithContentAlpha(
            animateFloatAsState(
                if (isWindowFocused) 1f else 0.5f
            ).value
        ) {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = myTextSizes.base,
                modifier = Modifier
                    .then(modifier)
            )
        }
    }
}

@Composable
internal fun CommonRenderTitleBar(
    modifier: Modifier,
    titleBar: TitleBar,
    title: String,
    windowIcon: Painter? = null,
    titlePosition: TitlePosition,
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
) {
    Row(
        modifier.height(titleBar.titleBarHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val systemButtonsAtFirst = titleBar.systemButtonsPosition.isLeft

        if (systemButtonsAtFirst) {
            titleBar.RenderSystemButtons(
                onRequestClose = onRequestClose,
                onRequestMinimize = onRequestMinimize,
                onToggleMaximize = onRequestToggleMaximize,
            )
        }
        titleBar.RenderTitleBarContent(
            title = title,
            titlePosition = titlePosition,
            modifier = Modifier.weight(1f),
            windowIcon = windowIcon,
            start = start,
            end = end
        )
        if (!systemButtonsAtFirst) {
            titleBar.RenderSystemButtons(
                onRequestClose = onRequestClose,
                onRequestMinimize = onRequestMinimize,
                onToggleMaximize = onRequestToggleMaximize,
            )
        }
    }
}
