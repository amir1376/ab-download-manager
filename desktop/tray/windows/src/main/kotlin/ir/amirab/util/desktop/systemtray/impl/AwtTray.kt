package ir.amirab.util.desktop.systemtray.impl

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.isTraySupported
import ir.amirab.util.desktop.GlobalDensity
import ir.amirab.util.desktop.GlobalLayoutDirection
import ir.amirab.util.desktop.trayIconSize
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

@Composable
internal fun AwtTray(
    tooltip: String,
    icon: Painter,
    onClick: () -> Unit,
    //it is a dp offset on the screen
    onRightClick: (DpOffset) -> Unit,
) {
    val density by rememberUpdatedState(GlobalDensity)
    if (!isTraySupported) {
        DisposableEffect(Unit) {
            System.err.println("System tray is not supported")
            onDispose {}
        }
        return
    }

    val currentOnAction by rememberUpdatedState(onClick)
    val currentRightClick by rememberUpdatedState(onRightClick)
    val awtIcon = remember(icon) {
        // We shouldn't use LocalDensity here because Tray's density doesn't equal it. It
        // equals to the density of the screen on which it shows. Currently Swing doesn't
        // provide us such information, it only requests an image with the desired width/height
        // (see MultiResolutionImage.getResolutionVariant). Resources like svg/xml should look okay
        // because they don't use absolute '.dp' values to draw, they use values which are
        // relative to their viewport.
        icon.toAwtImage(GlobalDensity, GlobalLayoutDirection, trayIconSize)
    }

    val trayIcon = remember {
        TrayIcon(awtIcon)
    }
    DisposableEffect(trayIcon) {
        with(trayIcon) {
            val doubleClickListener = object : MouseListener {
                override fun mouseClicked(p0: MouseEvent?) {
                    when (p0?.button) {
                        1 -> currentOnAction()
                        3 -> {
                            with(density) {
                                currentRightClick(
                                    DpOffset(p0.x.toDp(), p0.y.toDp())
                                )
                            }
                        }
                    }
                }

                override fun mousePressed(p0: MouseEvent?) {}
                override fun mouseReleased(p0: MouseEvent?) {}
                override fun mouseEntered(p0: MouseEvent?) {}
                override fun mouseExited(p0: MouseEvent?) {}
            }
            val actionListener = ActionListener {
                currentOnAction()
            }
            isImageAutoSize = true
            addMouseListener(doubleClickListener)
            addActionListener(actionListener)
            onDispose {
                removeActionListener(actionListener)
                removeMouseListener(doubleClickListener)
            }
        }
    }
    SideEffect {
        if (trayIcon.image != awtIcon) trayIcon.image = awtIcon
        if (trayIcon.toolTip != tooltip) trayIcon.toolTip = tooltip
    }

    DisposableEffect(Unit) {
        val systemTray = kotlin.runCatching {
            // some linux users reporting that system tray will not support by distro and throws exception after entering to lock screen
            // I have to investigate why!
            SystemTray.getSystemTray()
        }.getOrNull()

        systemTray?.add(trayIcon)

        onDispose {
            // don't get hard!
            // linux have some strange behaviors in system tray I have to improve this
            runCatching {
                systemTray?.remove(trayIcon)
            }
        }
    }
}
