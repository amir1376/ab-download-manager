package ir.amirab.util.desktop.systemtray.impl

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import ir.amirab.util.desktop.GlobalDensity
import ir.amirab.util.desktop.GlobalLayoutDirection
import ir.amirab.util.desktop.trayIconSize
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

@Composable
internal fun AwtTray(
    tooltip: String,
    icon: Painter,
    menuItems: List<ImmutableMenuItem>
) {
    // Check if System Tray is supported
    if (!SystemTray.isSupported()) {
        DisposableEffect(Unit) {
            System.err.println("System tray is not supported on this system")
            onDispose {}
        }
        return
    }

    val awtIcon = remember(icon) {
        icon.toAwtImage(GlobalDensity, GlobalLayoutDirection, trayIconSize)
    }

    val trayIcon = remember { TrayIcon(awtIcon) }
    val popupMenu = remember { PopupMenu() }

    DisposableEffect(trayIcon) {
        val systemTray = SystemTray.getSystemTray()
        if (systemTray == null) {
            System.err.println("System tray is not supported")
            return@DisposableEffect onDispose { }
        }

        // Add menu items to the popup menu
        popupMenu.addImmutableMenu(menuItems)

        trayIcon.isImageAutoSize = true
        trayIcon.toolTip = tooltip
        trayIcon.popupMenu = popupMenu

        // Add tray icon to the system tray
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