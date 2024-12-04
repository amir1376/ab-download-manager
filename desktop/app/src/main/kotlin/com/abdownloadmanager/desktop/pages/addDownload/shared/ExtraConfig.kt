package com.abdownloadmanager.desktop.pages.addDownload.shared

import com.abdownloadmanager.desktop.pages.settings.configurable.widgets.RenderConfigurable
import com.abdownloadmanager.desktop.ui.customwindow.BaseOptionDialog
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.utils.compose.WithContentColor
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.desktop.utils.windowUtil.moveSafe
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberDialogState
import com.abdownloadmanager.desktop.pages.settings.configurable.Configurable
import com.abdownloadmanager.desktop.ui.theme.LocalUiScale
import ir.amirab.util.desktop.screen.applyUiScale
import java.awt.Dimension
import java.awt.MouseInfo

@Composable
fun ExtraConfig(
    onDismiss: () -> Unit,
    configurables: List<Configurable<*>>,
) {
    val h = 250
    val w = 300
    val state = rememberDialogState(
        size = DpSize(
            height = h.dp,
            width = w.dp,
        ).applyUiScale(LocalUiScale.current),
    )
    BaseOptionDialog(onDismiss, state) {
        LaunchedEffect(window){
            window.moveSafe(
                MouseInfo.getPointerInfo().location.run {
                    DpOffset(
                        x = x.dp,
                        y = y.dp
                    )
                }
            )
        }


        val shape = RoundedCornerShape(6.dp)
        Column(
            Modifier
                .fillMaxSize()
                .clip(shape)
                .border(2.dp, myColors.onBackground / 10, shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            myColors.surface,
                            myColors.background,
                        )
                    )
                )
        ) {
            WithContentColor(myColors.onBackground) {
                LaunchedEffect(w, h) {
                    window.minimumSize = Dimension(w, h)
                }
                Column {
                    WindowDraggableArea(Modifier.fillMaxWidth()) {
                        Text(
                            "Extra Config", Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                                .wrapContentWidth()
                        )
                    }
                    Divider()
                    Box {
                        val scrollState = rememberScrollState()
                        Column(
                            Modifier.verticalScroll(scrollState)
                        ) {
                            for ((index, cfg) in configurables.withIndex()) {
                                RenderConfigurable(
                                    cfg,
                                    Modifier.padding(vertical = 8.dp, horizontal = 32.dp)
                                )
                                if (index != configurables.lastIndex) {
                                    Divider()
                                }
                            }
                        }
                        VerticalScrollbar(
                            rememberScrollbarAdapter(scrollState),
                            Modifier.fillMaxHeight()
                                .align(Alignment.CenterEnd)
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun Divider() {
    Spacer(
        Modifier.fillMaxWidth()
            .height(1.dp)
            .background(myColors.onBackground / 10),
    )
}
