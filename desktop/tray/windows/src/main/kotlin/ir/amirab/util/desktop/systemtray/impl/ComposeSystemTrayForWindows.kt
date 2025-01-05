package ir.amirab.util.desktop.systemtray.impl

import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.abdownloadmanager.shared.ui.widget.menu.SubMenu
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.desktop.systemtray.IComposeSystemTray
import com.abdownloadmanager.desktop.window.custom.BaseOptionDialog
import com.abdownloadmanager.desktop.window.moveSafe
import com.google.auto.service.AutoService

@AutoService(IComposeSystemTray::class)
class ComposeSystemTrayForWindows : IComposeSystemTray {
    @Composable
    override fun ComposeSystemTray(
        icon: IconSource,
        title: StringSource,
        menu: List<MenuItem>,
        onClick: () -> Unit,
    ) {
        var popUpPosition by remember { mutableStateOf(null as DpOffset?) }
        val closeOptions = { popUpPosition = null }
        AwtTray(
            tooltip = title.rememberString(),
            icon = icon.rememberPainter(),
            onClick = onClick,
            onRightClick = {
                popUpPosition = it
            }
        )
        popUpPosition.let { position ->
            if (position != null) {
                TrayOptions(
                    position,
                    closeOptions,
                ) {
                    SubMenu(menu, closeOptions)
                }
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
            LaunchedEffect(window) {
                window.moveSafe(position)
            }
            content()
        }
    )
}
