package ir.amirab.util.desktop.systemtray.impl

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import ir.amirab.util.desktop.GlobalDensity
import ir.amirab.util.desktop.GlobalLayoutDirection
import ir.amirab.util.desktop.trayIconSize
import java.awt.*

@Composable
internal fun AwtTray(
    tooltip: String,
    icon: Painter,
    menuItems: List<ImmutableMenuItem>
) {
    // Check if System Tray is supported
    if (!SystemTray.isSupported()) {
        return
    }

    val awtIcon = remember(icon) {
        icon.toAwtImage(GlobalDensity, GlobalLayoutDirection, trayIconSize)
    }

    val trayIcon = remember { TrayIcon(awtIcon) }
    val popupMenu = remember { PopupMenu() }

    DisposableEffect(trayIcon) {
        val systemTray = SystemTray.getSystemTray()
        if (systemTray == null) return@DisposableEffect onDispose {}

        popupMenu.addImmutableMenu(menuItems)
        trayIcon.isImageAutoSize = true
        trayIcon.toolTip = tooltip
        trayIcon.popupMenu = popupMenu


        systemTray.add(trayIcon)

        onDispose {
            systemTray.remove(trayIcon)
        }
    }
}


private fun PopupMenu.addImmutableMenu(immutableMenu: List<ImmutableMenuItem>) {
    for (item in immutableMenu) {
        when (item) {
            ImmutableMenuItem.Separator -> addSeparator()
            is ImmutableMenuItem.SingleItem -> add(
                MenuItem(
                    item.title,
                ).apply {
                    addActionListener {
                        item.onAction()
                    }
                }
            )

            is ImmutableMenuItem.SubMenu -> add(
                Menu(
                    item.title,
                ).apply {
                    addImmutableMenu(item.items)
                }
            )
        }
    }
}