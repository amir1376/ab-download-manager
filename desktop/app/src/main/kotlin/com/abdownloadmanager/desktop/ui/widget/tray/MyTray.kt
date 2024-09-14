package com.abdownloadmanager.desktop.ui.widget.tray

import com.abdownloadmanager.desktop.ui.customwindow.BaseOptionDialog
import com.abdownloadmanager.desktop.ui.widget.menu.SubMenu
import ir.amirab.util.compose.action.MenuItem
import com.abdownloadmanager.desktop.utils.windowUtil.moveSafe
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import ir.amirab.util.desktop.systemtray.AwtTray

@Composable
fun ComposeTray(
    tooltip: String,
    icon: Painter,
    state: TrayState = rememberTrayState(),
    onClick: () -> Unit,
    menu: List<MenuItem>,
) {
    var popUpPosition by remember { mutableStateOf(null as DpOffset?) }
    val closeOptions = { popUpPosition = null }
    AwtTray(
        tooltip = tooltip,
        icon = icon,
        state = state,
        onClick = onClick,
        onRightClick = {
            popUpPosition = it
        }
    )
    popUpPosition.let { position ->
        if(position!=null){
            TrayOptions(
                position,
                closeOptions,
            ) {
                SubMenu(menu,closeOptions)
            }
        }
    }
}

@Composable
private fun TrayOptions(
    position: DpOffset,
    onRequestClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    val state = rememberDialogState(
        size = DpSize.Unspecified,
        position = WindowPosition.Absolute(
            x = position.x,
            y = position.y,
        )
    )

    BaseOptionDialog(
        onCloseRequest = onRequestClose,
        resizeable = false,
        state = state,
        content = {
            LaunchedEffect(window){
                window.moveSafe(position)
            }
            content()
        }
    )
}
